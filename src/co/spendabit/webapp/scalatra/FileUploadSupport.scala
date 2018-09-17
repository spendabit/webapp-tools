package co.spendabit.webapp.scalatra

import co.spendabit.webapp.{MultipartFormHandling, UploadConfig}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.apache.commons.fileupload.FileItem
import org.scalatra._
import org.scalatra.servlet.ServletBase

/** An alternative mechanism (to Scalatra's built-in mechanism) for Scalatra-based servlets to
  * handle file-uploads, leveraging the Apache 'commons-fileupload' package. The primary motivation
  * here is in avoiding the need to have "max file size" (and et al) configuration in web.xml or
  * elsewhere, which has always proven a major PITA for this class's author; the "configuration"
  * is provided merely by implementing the 'uploadConfig' val/method on the sub-class servlet.
  */
trait FileUploadSupport extends ServletBase with MultipartFormHandling {

  protected val uploadConfig: UploadConfig

  protected def uploadedFile(paramName: String): Option[FileItem] =
    fileItems.find(_.getFieldName == paramName)

  override def handle(req: HttpServletRequest, res: HttpServletResponse) {

    if (isMultipartRequest(req)) {

      val items = readMultipartFormData(req, uploadConfig)
      fileItems = items.filter(!_.isFormField)

      val mp: MultiParams = items.filter(_.isFormField).
        map(i => (i.getFieldName, Seq(i.getString))).toMap
      req.setAttribute("MultiParamsRead", new {})
      req.setAttribute(MultiParamsKey, mp)
    }

    super.handle(req, res)
  }

  // XXX: This mechanism is broken; it would not be thread-safe, and could lead to submitted
  // XXX: data being "mixed up" if multiple requests came in at roughly the same time.
  private var fileItems: Seq[FileItem] = Seq()
}
