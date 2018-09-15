package co.spendabit.webapp.forms.controls

import org.apache.commons.fileupload.FileItem

abstract class Field[T](val label: String) {
  def validate(params: Map[String, Seq[String]], fileItems: Seq[FileItem]): Either[String, T]
  def widgetHTML(value: Option[T] = None): xml.NodeSeq
}
