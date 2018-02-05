package org.eclipse.jdt.internal.compiler.batch;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.IModule;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.Util;

public class ClasspathJep247 extends ClasspathLocation {

	private java.nio.file.FileSystem fs = null;
	private String release = null;
	private String[] subReleases = null;
	private Path releasePath = null;
	private File file = null;
	private Set<String> packageCache;
	
	public ClasspathJep247(File jdkHome, String release, AccessRuleSet accessRuleSet) {
		super(accessRuleSet, null);
		this.release = release;
		this.file = jdkHome;
	}
	@Override
	public List<Classpath> fetchLinkedJars(FileSystem.ClasspathSectionProblemReporter problemReporter) {
		 return null;
	}
	@Override
	public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String moduleName, String qualifiedBinaryFileName) {
		return findClass(typeName, qualifiedPackageName, moduleName, qualifiedBinaryFileName, false);
	}
	@Override
	public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String moduleName, String qualifiedBinaryFileName, boolean asBinaryOnly) {
		if (!isPackage(qualifiedPackageName, moduleName))
			return null; // most common case

		try {
			//TODO: Check if any conversion needed for path separator
			ClassFileReader reader = null;
			byte[] content = null;
			qualifiedBinaryFileName = qualifiedBinaryFileName.replace(".class", ".sig"); //$NON-NLS-1$ //$NON-NLS-2$
			if (this.subReleases != null && this.subReleases.length > 0) {
				for (String rel : this.subReleases) {
					Path p = this.fs.getPath(rel, qualifiedBinaryFileName);
					if (Files.exists(p)) {
						content = Files.readAllBytes(p);
						if (content != null) 
							break;
					}
				}
			} else {
				content = Files.readAllBytes(this.fs.getPath(this.release, qualifiedBinaryFileName));
			}
			if (content != null) {
				reader = new ClassFileReader(content, qualifiedBinaryFileName.toCharArray());
				return new NameEnvironmentAnswer(reader, fetchAccessRestriction(qualifiedBinaryFileName), null);
			}
		} catch(ClassFormatException e) {
			// Continue
		} catch (IOException e) {
			// continue
		}
		return null;
	}
	@Override
	public boolean hasAnnotationFileFor(String qualifiedTypeName) {
		return false;
	}
	@Override
	public char[][][] findTypeNames(final String qualifiedPackageName, String moduleName) {
		// TODO: Revisit
		return null;
	}

	@Override
	public void initialize() throws IOException {
		if (this.release == null) {
			return;
		}
		Path filePath = this.file.toPath().resolve("lib").resolve("ct.sym"); //$NON-NLS-1$ //$NON-NLS-2$
		URI t = filePath.toUri();
		if (!Files.exists(filePath)) {
			return;
		}
		URI uri = URI.create("jar:file:" + t.getPath()); //$NON-NLS-1$
		try {
			this.fs = FileSystems.getFileSystem(uri);
		} catch(FileSystemNotFoundException fne) {
			// Ignore and move on
		}
		if (this.fs == null) {
			HashMap<String, ?> env = new HashMap<>();
			this.fs = FileSystems.newFileSystem(uri, env);
		}
		this.releasePath = this.fs.getPath(""); //$NON-NLS-1$
		if (!Files.exists(this.fs.getPath(this.release))) {
			throw new IllegalArgumentException("release " + this.release + " is not found in the system");  //$NON-NLS-1$//$NON-NLS-2$
		}
	}
	void acceptModule(ClassFileReader reader) {
		// Nothing to do
	}
	protected void addToPackageCache(String packageName, boolean endsWithSep) {
		if (this.packageCache.contains(packageName))
			return;
		this.packageCache.add(packageName);
	}
	@Override
	public synchronized char[][] getModulesDeclaringPackage(String qualifiedPackageName, String moduleName) {
		// Ignore moduleName as this has nothing to do with modules (as of now)
		if (this.packageCache != null)
			return singletonModuleNameIf(this.packageCache.contains(qualifiedPackageName));

		this.packageCache = new HashSet<>(41);
		this.packageCache.add(Util.EMPTY_STRING);
		List<String> sub = new ArrayList<>();
		try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(this.releasePath)) {
			for (final java.nio.file.Path subdir: stream) {
				String rel = subdir.getFileName().toString();
				if (rel.contains(this.release)) {
					sub.add(rel);
				} else {
					continue;
				}
				Files.walkFileTree(subdir, new FileVisitor<java.nio.file.Path>() {
					@Override
					public FileVisitResult preVisitDirectory(java.nio.file.Path dir, BasicFileAttributes attrs) throws IOException {
						if (dir.getNameCount() <= 1)
							return FileVisitResult.CONTINUE;
						Path relative = dir.subpath(1, dir.getNameCount());
						addToPackageCache(relative.toString(), false);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(java.nio.file.Path f, BasicFileAttributes attrs) throws IOException {
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFileFailed(java.nio.file.Path f, IOException exc) throws IOException {
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(java.nio.file.Path dir, IOException exc) throws IOException {
						return FileVisitResult.CONTINUE;
					}
				});
			}
		} catch (IOException e) {
			e.printStackTrace();
			// Rethrow
		}
		this.subReleases = sub.toArray(new String[sub.size()]);
		return singletonModuleNameIf(this.packageCache.contains(qualifiedPackageName));
	}
	@Override
	public boolean hasCompilationUnit(String qualifiedPackageName, String moduleName) {
		// TOOD: Revisit
		return false;
	}
	@Override
	public void reset() {
		try {
			this.fs.close();
		} catch (IOException e) {
			// Move on
		}
	}
	@Override
	public String toString() {
		return "Classpath for JEP 247 for JDK " + this.file.getPath(); //$NON-NLS-1$
	}
	@Override
	public char[] normalizedPath() {
		if (this.normalizedPath == null) {
			String path2 = this.getPath();
			char[] rawName = path2.toCharArray();
			if (File.separatorChar == '\\') {
				CharOperation.replace(rawName, '\\', '/');
			}
			this.normalizedPath = CharOperation.subarray(rawName, 0, CharOperation.lastIndexOf('.', rawName));
		}
		return this.normalizedPath;
	}
	@Override
	public String getPath() {
		if (this.path == null) {
			try {
				this.path = this.file.getCanonicalPath();
			} catch (IOException e) {
				// in case of error, simply return the absolute path
				this.path = this.file.getAbsolutePath();
			}
		}
		return this.path;
	}
	@Override
	public int getMode() {
		return BINARY;
	}

	@Override
	public IModule getModule() {
		return null;
	}
}
