package co.spendabit.webapp.forms.controls

import org.apache.commons.fileupload.FileItem

class FileUploadInput(label: String, name: String)
        extends Field[FileItem](label) {

  def validate(params: Map[String, Seq[String]],
               fileItems: Seq[FileItem]): Either[String, FileItem] = {
    fileItems.find(_.getFieldName == name) match {
      case Some(fi) => Right(fi)
      case None => Left("Please choose a file to upload.")
    }
  }

  def widgetHTML(value: Option[FileItem] = None): xml.NodeSeq =
    <input type="file" name={ name } />
}

object FileUploadInput {
  def apply(label: String, name: String) = new FileUploadInput(label, name)
}
