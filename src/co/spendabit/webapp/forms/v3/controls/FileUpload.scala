package co.spendabit.webapp.forms.v3.controls

import org.apache.commons.fileupload.FileItem

import scala.xml.NodeSeq

class FileUpload extends FileBasedInput[FileItem] {

  def validate(fileItem: Option[FileItem]): Either[String, FileItem] = {
    fileItem match {
      case Some(fi) => Right(fi)
      case None => Left("Please choose a file to upload.")
    }
  }

  override def html(value: Option[FileItem]): NodeSeq =
    <input type="file" />
}

object FileUpload {
  def apply() = new FileUpload
}
