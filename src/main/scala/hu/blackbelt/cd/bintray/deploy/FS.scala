package hu.blackbelt.cd.bintray

import com.google.common.jimfs.{Configuration, Jimfs}

object VFS {
  private val SIZE_512_M: Long = 512L * 1204 * 1024
  val FS = Jimfs.newFileSystem(Configuration.unix().toBuilder.setMaxSize(SIZE_512_M).build());
}
