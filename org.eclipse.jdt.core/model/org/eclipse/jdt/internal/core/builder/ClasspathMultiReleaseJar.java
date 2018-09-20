package org.eclipse.jdt.internal.core.builder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.ProviderNotFoundException;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ExternalAnnotationDecorator;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding.ExternalAnnotationStatus;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;

public class ClasspathMultiReleaseJar extends ClasspathJar {
	private java.nio.file.FileSystem fs = null;
	Path releasePath = null;
	Path rootPath = null;
	Path[] supportedVersions;

	ClasspathMultiReleaseJar(IFile resource, AccessRuleSet accessRuleSet, IPath externalAnnotationPath,
			boolean isOnModulePath, String compliance) {
		super(resource, accessRuleSet, externalAnnotationPath, isOnModulePath);
		this.compliance = compliance;
		initializeVersions();
	}

	ClasspathMultiReleaseJar(String zipFilename, long lastModified, AccessRuleSet accessRuleSet,
			IPath externalAnnotationPath, boolean isOnModulePath, String compliance) {
		super(zipFilename, lastModified, accessRuleSet, externalAnnotationPath, isOnModulePath);
		this.compliance = compliance;
		initializeVersions();
	}

	public ClasspathMultiReleaseJar(ZipFile zipFile, AccessRuleSet accessRuleSet, IPath externalAnnotationPath,
			boolean isOnModulePath, String compliance) {
		this(zipFile.getName(), accessRuleSet, externalAnnotationPath, isOnModulePath, compliance);
		this.zipFile = zipFile;
		this.closeZipFileAtEnd = true;
	}

	public ClasspathMultiReleaseJar(String fileName, AccessRuleSet accessRuleSet, IPath externalAnnotationPath,
			boolean isOnModulePath, String compliance) {
		this(fileName, 0, accessRuleSet, externalAnnotationPath, isOnModulePath, compliance);
		if (externalAnnotationPath != null)
			this.externalAnnotationPath = externalAnnotationPath.toString();
	}

	@Override
	IModule initializeModule() {
		IModule mod = null;
		ZipFile file = null;
		try {
			file = new ZipFile(this.zipFilename);
			ClassFileReader classfile = null;
			try {
				for (Path path : this.supportedVersions) {
					classfile = ClassFileReader.read(file, path.toString() + '/' + IModule.MODULE_INFO_CLASS);
					if (classfile != null)
						break;
				}

			} catch (Exception e) {
				e.printStackTrace();
				// move on to the default
			}
			if (classfile == null) {
				classfile = ClassFileReader.read(file, IModule.MODULE_INFO_CLASS); // FIXME: use jar cache
			}
			if (classfile != null) {
				mod = classfile.getModuleDeclaration();
			}
		} catch (ClassFormatException | IOException e) {
			// do nothing
		} finally {
			try {
				if (file != null)
					file.close();
			} catch (IOException e) {
				// do nothing
			}
		}
		return mod;
	}

	private void initializeVersions() {
		Path filePath = Paths.get(this.zipFilename);
		if (Files.exists(filePath)) {
			URI uri = URI.create("jar:" + filePath.toUri()); //$NON-NLS-1$
			try {
				try {
					this.fs = FileSystems.getFileSystem(uri);
				} catch (FileSystemNotFoundException e) {
					// move on
				}
				if (this.fs == null) {
					HashMap<String, ?> env = new HashMap<>();
					this.fs = FileSystems.newFileSystem(uri, env);
				}
			} catch (FileSystemNotFoundException | ProviderNotFoundException e) {
				// move on
			} catch (IOException e) {
				// move on
			}
			if (this.fs == null)
				return;
			this.rootPath = this.fs.getPath("/"); //$NON-NLS-1$
			int earliestJavaVersion = ClassFileConstants.MAJOR_VERSION_9;
			long latestJDK = CompilerOptions.releaseToJDKLevel(this.compliance);
			int latestJavaVer = (int) (latestJDK >> 16);
			List<Path> versions = new ArrayList<>();
			for (int i = latestJavaVer; i >= earliestJavaVersion; i--) {
				Path path = this.fs.getPath("/", "META-INF", "versions", "" + (i - 44)); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				if (Files.exists(path)) {
					versions.add(this.rootPath.relativize(path));
				}
			}
			this.supportedVersions = versions.toArray(new Path[versions.size()]);
			if (this.supportedVersions.length <= 0) {
				try {
					this.fs.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}

	@Override
	protected String readJarContent(final SimpleSet packageSet) {
		String[] modInfo = new String[1];
		modInfo[0] = super.readJarContent(packageSet);
		try {
			for (Path path : this.supportedVersions) {
				Path relativePath = this.rootPath.resolve(path);
				Files.walkFileTree(path, new FileVisitor<java.nio.file.Path>() {
					@Override
					public FileVisitResult preVisitDirectory(java.nio.file.Path dir, BasicFileAttributes attrs)
							throws IOException {
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs)
							throws IOException {
						Path p = relativePath.relativize(file);
						addToPackageSet(packageSet, p.toString(), false);
						if (modInfo[0] == null) {
							if (p.getFileName().toString().equalsIgnoreCase(IModule.MODULE_INFO_CLASS)) {
								modInfo[0] = relativePath.relativize(file).toString();
							}
						}
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFileFailed(java.nio.file.Path file, IOException exc)
							throws IOException {
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(java.nio.file.Path dir, IOException exc)
							throws IOException {
						return FileVisitResult.CONTINUE;
					}
				});
			}
		} catch (Exception e) {
			// move on;
		}
		return modInfo[0];
	}

	@Override
	public NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String moduleName,
			String qualifiedBinaryFileName, boolean asBinaryOnly, Predicate<String> moduleNameFilter) {
		if (!isPackage(qualifiedPackageName, moduleName))
			return null; // most common case
		for (Path path : this.supportedVersions) {
			Path relativePath = this.rootPath.resolve(path);
			try {
				Path p = relativePath.resolve(qualifiedPackageName).resolve(binaryFileName);
				if (!Files.exists(p))
					continue;
				byte[] content = Files.readAllBytes(p);
				IBinaryType reader = null;
				if (content != null) {
					reader = new ClassFileReader(content, qualifiedBinaryFileName.toCharArray());
				}
				if (reader != null) {
					char[] modName = this.module == null ? null : this.module.name();
					if (reader instanceof ClassFileReader) {
						ClassFileReader classReader = (ClassFileReader) reader;
						if (classReader.moduleName == null)
							classReader.moduleName = modName;
						else
							modName = classReader.moduleName;
					}
					String fileNameWithoutExtension = qualifiedBinaryFileName.substring(0,
							qualifiedBinaryFileName.length() - SuffixConstants.SUFFIX_CLASS.length);
					if (this.externalAnnotationPath != null) {
						try {
							if (this.annotationZipFile == null) {
								this.annotationZipFile = ExternalAnnotationDecorator
										.getAnnotationZipFile(this.externalAnnotationPath, null);
							}

							reader = ExternalAnnotationDecorator.create(reader, this.externalAnnotationPath,
									fileNameWithoutExtension, this.annotationZipFile);
						} catch (IOException e) {
							// don't let error on annotations fail class reading
						}
						if (reader.getExternalAnnotationStatus() == ExternalAnnotationStatus.NOT_EEA_CONFIGURED) {
							// ensure a reader that answers NO_EEA_FILE
							reader = new ExternalAnnotationDecorator(reader, null);
						}
					}
					if (this.accessRuleSet == null)
						return new NameEnvironmentAnswer(reader, null, modName);
					return new NameEnvironmentAnswer(reader,
							this.accessRuleSet.getViolatedRestriction(fileNameWithoutExtension.toCharArray()), modName);
				}
			} catch (IOException | ClassFormatException e) {
				e.printStackTrace();
				// treat as if class file is missing
			}
		}
		return super.findClass(binaryFileName, qualifiedPackageName, moduleName, qualifiedBinaryFileName, asBinaryOnly,
				moduleNameFilter);
	}
}
