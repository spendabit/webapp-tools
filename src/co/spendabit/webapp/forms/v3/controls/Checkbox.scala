package co.spendabit.webapp.forms.v3.controls

import co.spendabit.webapp.forms.util.withAttr

object Checkbox extends TextBasedInput[Boolean] {

  override def html(value: Option[Boolean] = None): xml.NodeSeq = {
//    val cb = <input type="checkbox" name={ name } />
    val cb = <input type="checkbox" />
    if (value.contains(true)) withAttr(cb, "checked", "checked") else cb
  }

  def validate(s: String): Either[String, Boolean] =
    Right(s == "on")
//  def validate(params: Map[String, Seq[String]]): Either[String, Boolean] =
//    Right(
//      params.get(name) match {
//        case Some(Seq("on")) => true
//        case _               => false
//      })
}
