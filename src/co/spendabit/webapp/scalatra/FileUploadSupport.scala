package co.spendabit.webapp.scalatra

import javax.servlet.http.{HttpServletResponse, HttpServletRequest}

import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.scalatra._
import org.scalatra.servlet.ServletBase
import org.scalatra.util.MultiMap

/** An alternative mechanism (to Scalatra's built-in mechanism) for Scalatra-based servlets to
  * handle file-uploads, leveraging the Apache 'commons-fileupload' package. The primary motivation
  * here is in avoiding the need to have "max file size" (and et al) configuration in web.xml or
  * elsewhere, which has always proven a major PITA for this class's author; the "configuration"
  * is provided merely by implementing the 'uploadConfig' val/method on the sub-class servlet.
  */
trait FileUploadSupport extends ServletBase {

  case class UploadConfig(maxFileSize: Long, saveToDiskThreshold: Int)

  protected val uploadConfig: UploadConfig

  protected def uploadedFile(paramName: String): Option[FileItem] =
    fileItems.find(_.getFieldName == paramName)

  override def handle(req: HttpServletRequest, res: HttpServletResponse) {

    if (isMultipartRequest(req)) {

      val factory = new DiskFileItemFactory
      factory.setSizeThreshold(uploadConfig.saveToDiskThreshold)
      factory.setRepository(temporaryDirectory)

      // Create a new file-upload handler...
      val uploadProcessor = new ServletFileUpload(factory)
      uploadProcessor.setFileSizeMax(uploadConfig.maxFileSize)

      // And parse the request.
      import scala.collection.JavaConversions._
      fileItems = uploadProcessor.parseRequest(req)

      val mp: MultiParams = MultiMap(fileItems.map(i => (i.getFieldName, Seq(i.getString))).toMap)
      req.setAttribute("MultiParamsRead", new {})
      req.setAttribute(MultiParamsKey, mp)
    }

    super.handle(req, res)
  }

  private var fileItems: Seq[FileItem] = Seq()

  private def isMultipartRequest(req: HttpServletRequest): Boolean = {
    val isPostOrPut = Set("POST", "PUT", "PATCH").contains(req.getMethod)
    isPostOrPut && (req.contentType match {
      case Some(contentType) => contentType.startsWith("multipart/")
      case _ => false
    })
  }

  private def temporaryDirectory: java.io.File =
    Option(servletContext.getAttribute("javax.servlet.context.tempdir")).
      map(_.asInstanceOf[java.io.File]).
      getOrElse(new java.io.File(System.getProperty("java.io.tmpdir")))
}
