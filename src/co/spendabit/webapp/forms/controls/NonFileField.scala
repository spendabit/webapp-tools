package co.spendabit.webapp.forms.controls

import org.apache.commons.fileupload.FileItem

/** Any field which will be submitted as one or more string-based parameters. That is, pretty
  * much everything other than file-upload fields (<input type="file" />).
  */
abstract class NonFileField[T](label: String) extends Field[T](label) {

  def validate(params: Map[String, Seq[String]]): Either[String, T]

  def validate(params: Map[String, Seq[String]], fileItems: Seq[FileItem]): Either[String, T] =
    validate(params)
}
