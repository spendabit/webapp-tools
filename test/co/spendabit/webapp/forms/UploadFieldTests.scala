package co.spendabit.webapp.forms

import java.io.{File, InputStream}
import scala.util.Random
import co.spendabit.test.scalatra.AdvancedWebBrowsing
import co.spendabit.webapp.UploadConfig
import co.spendabit.webapp.forms.controls.{EmailField, FileUploadInput, TextInput}
import co.spendabit.webapp.forms.ui.bootstrap
import co.spendabit.webapp.forms.v2._
import co.spendabit.webapp.forms.v3.{DefaultFormRenderer, Field}
import javax.mail.internet.InternetAddress
import org.apache.commons.fileupload.FileItem
import org.scalatest.FunSuite
import org.scalatra.ScalatraServlet

class UploadFieldTests extends FunSuite with AdvancedWebBrowsing with FormTestHelpers {

  addServlet(testServlet, "/*")

  private val fileToUpload = new org.scalatra.test.Uploadable {
    private val inStream = resourceAsInputStream(new File("mog-with-wine.jpg"))
    val content: Array[Byte] = toByteArray(inStream)
    val fileName: String = "mog-with-wine.jpg"
    val contentType: String = "image/jpeg"
    val contentLength: Long = content.length
  }

  test("functionality of `FileUploadInput`, v2") {

    get("/upload-form-v2") {
      post("/upload-form-v2", params = Seq("your-email" -> "mog@fantasy.net"),
           files = Seq("photo" -> fileToUpload)) {
        assert(status == 200)
      }
    }
  }

  test("functionality of `FileUploadInput`, v3") {

    get("/upload-form-v3") {
      post("/upload-form-v3", params = Seq("email-address" -> "mog@fantasy.net"),
           files = Seq("photograph" -> fileToUpload)) {
        assert(status == 200)
      }
    }
  }

  protected def toByteArray(is: InputStream) =
    Stream.continually(is.read()).takeWhile(_ != -1).map(_.toByte).toArray

  protected def resourceAsInputStream(path: File) = {
    val inStream = Thread.currentThread.getContextClassLoader.getResourceAsStream(path.toString)
    if (inStream == null)
      throw new IllegalArgumentException(s"Could not find resource at path $path")
    inStream
  }

  lazy val testServlet = new ScalatraServlet {

    private val uploadFormV2 = new WebForm2[InternetAddress, FileItem] {

      override protected def uploadConfig = Some(
        UploadConfig(maxFileSize = 1024 * 1024,
          saveToDiskThreshold = fileToUpload.contentLength.toInt +
            Random.nextInt(500) - Random.nextInt(500)))

      def method = POST

      def action = "/upload-form-v2"

      def fields =
        (EmailField(label = "Email address", name = "your-email"),
          FileUploadInput(label = "Your photo", name = "photo"))
    }

    get("/upload-form-v2") {
      <html>
        <body>
          { uploadFormV2.html }
        </body>
      </html>
    }

    post("/upload-form-v2") {

      contentType = "text/plain"

      uploadFormV2.validate(request) match {
        case Valid((email, photo)) =>
          assert(photo.getContentType == fileToUpload.contentType)
          assert(photo.getSize == fileToUpload.contentLength)
          assert(photo.getString.take(10).contains("JFIF"))
          assert(email.getAddress == "mog@fantasy.net")
          status = 200
          "Got it!"
        case Invalid(errors) =>
          status = 400
          s"No good! Errors: ${errors.mkString(", ")}"
      }
    }

    private val uploadFormV3 =
      new v3.WebForm2(
        v3.Field("Email address", v3.controls.EmailAddr),
        v3.Field("Photograph", v3.controls.FileUpload()))
      {
        override protected def uploadConfig: Option[UploadConfig] = Some(
          UploadConfig(maxFileSize = 1024 * 1024,
            saveToDiskThreshold = fileToUpload.contentLength.toInt +
              Random.nextInt(500) - Random.nextInt(500)))
      }

    get("/upload-form-v3") {
      val formHtml = uploadFormV3.html(action = "/upload-form-v3", method = "post",
        new DefaultFormRenderer[Field])
      <html><body>{ formHtml }</body></html>
    }

    post("/upload-form-v3") {

      contentType = "text/plain"

      uploadFormV3.validate(request) match {
        case Valid((email, photo)) =>
          assert(photo.getContentType == fileToUpload.contentType)
          assert(photo.getSize == fileToUpload.contentLength)
          assert(photo.getString.take(10).contains("JFIF"))
          assert(email.getAddress == "mog@fantasy.net")
          status = 200
          "Got it!"
        case Invalid(errors) =>
          status = 400
          s"No good! Errors: ${errors.mkString(", ")}"
      }
    }
  }

  test("form is given proper 'enctype'") {

    val formWithoutFileInput = new PostWebForm[String] with WebForm1[String] {
      def fields = TextInput(label = "Enter a value", name = "the-value")
    }
    val f1 = formWithoutFileInput.html.asInstanceOf[xml.Elem]
    assert(f1.label == "form")
    getAttr(f1, "enctype").foreach { enc => assert(enc != "multipart/form-data") }

    val formWithFileInput = new PostWebForm[FileItem] with WebForm1[FileItem] {
      def fields = FileUploadInput(label = "Upload a file", name = "f")
    }
    val f2 = formWithFileInput.html.asInstanceOf[xml.Elem]
    assert(getAttr(f2, "enctype").contains("multipart/form-data"))

    // And ensure the proper 'enctype' even when rendering using a custom renderer...
    val renderer = new bootstrap.HorizontalForm
    val renderedWithout = formWithoutFileInput.html(renderer).asInstanceOf[xml.Elem]
    getAttr(renderedWithout, "enctype").foreach { enc =>
      assert(enc != "multipart/form-data")
    }
    val renderedWith = formWithFileInput.html(renderer).asInstanceOf[xml.Elem]
    assert(getAttr(renderedWith, "enctype").contains("multipart/form-data"))
  }
}
