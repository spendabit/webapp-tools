package co.spendabit.webapp.forms.controls

import java.time.ZonedDateTime
import scala.xml.Elem

case class DateTimeInput(override val label: String, name: String)
        extends co.spendabit.webapp.forms.controls.NonFileField[ZonedDateTime](label) {

  def widgetHTML(value: Option[ZonedDateTime] = None): Elem =
    <div class="input-group">
      <input type="text" class="form-control" name={ name }
             value={ value.map(_.toString).getOrElse("") } />
      <div class="input-group-addon">UTC</div>
    </div>

  def validate(params: Map[String, Seq[String]]): Either[String, ZonedDateTime] =
    try Right(ZonedDateTime.parse(params(name).head.replace(" ", "T") + "+00:00"))
    catch { case _: Exception =>
      Left("Please provide a date/time formatted like this: 2018-06-10 16:45")
    }
}
