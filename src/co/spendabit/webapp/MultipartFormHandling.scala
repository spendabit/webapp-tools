package co.spendabit.webapp

import scala.collection.JavaConverters._

import javax.servlet.http.HttpServletRequest
import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.servlet.ServletFileUpload

/** General-purpose stuff related to handling file-uploads, used both by forms library and
  * Scalatra support for handling uploads at the servlet level.
  */
trait MultipartFormHandling {

  case class UploadConfig(maxFileSize: Long, saveToDiskThreshold: Int)

  protected def readMultipartFormData(req: HttpServletRequest,
                                      uploadConfig: UploadConfig): Seq[FileItem] = {

    if (!isMultipartRequest(req))
      throw new IllegalArgumentException("Given request does not have 'multipart' Content-Type")

    val factory = new DiskFileItemFactory
    factory.setSizeThreshold(uploadConfig.saveToDiskThreshold)
    factory.setRepository(temporaryDirectory(req))

    // Create a new file-upload handler...
    val uploadProcessor = new ServletFileUpload(factory)
    uploadProcessor.setFileSizeMax(uploadConfig.maxFileSize)

    uploadProcessor.parseRequest(req).asScala
  }

  protected def isMultipartRequest(req: HttpServletRequest): Boolean = {
    req.getServletContext
    val isPostOrPut = Set("POST", "PUT", "PATCH").contains(req.getMethod)
    isPostOrPut && (Option(req.getContentType) match {
      case Some(contentType) => contentType.startsWith("multipart/")
      case _ => false
    })
  }

  private def temporaryDirectory(req: HttpServletRequest): java.io.File =
    Option(req.getServletContext.getAttribute("javax.servlet.context.tempdir")).
      map(_.asInstanceOf[java.io.File]).
      getOrElse(new java.io.File(System.getProperty("java.io.tmpdir")))
}
