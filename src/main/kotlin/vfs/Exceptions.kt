package vfs

import java.lang.RuntimeException
import java.net.URI

class FileIsNotDirectoryException(file: VirtualFile) : RuntimeException("File ${file.getName()} is not a directory!")
class FileIsNotInValidStateException(file: VirtualFile) : RuntimeException("File ${file.getName()} is not in a valid state, i.e. is deleted or not loaded!")
class FileFromWrongFilesystemException(file: VirtualFile) : RuntimeException("File ${file.getName()} comes from different filesystem!")
class FileAlreadyExistsException(file: URI) : RuntimeException("File $file already exists, cannot perform operation" + TODO())
class IncorrectFileNameException(name: String) : RuntimeException("Name $name is not a correct name for a single file or directory!")
class FileCreationFailedException(file: URI) : RuntimeException("Creation of file with name $file failed!")
class UnsupportedResourceSchemaException(schema: String) : RuntimeException("Resource schema $schema is not supported!")