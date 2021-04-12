package co.spendabit.webapp.forms.v3.controls

import java.time.ZonedDateTime

object DateTimeInput extends GenericInput[ZonedDateTime] {

//  def widgetHTML(value: Option[ZonedDateTime] = None): Elem =
//    <div class="input-group">
//      <input type="text" class="form-control" name={ name }
//             value={ value.map(_.toString).getOrElse("") } />
//      <div class="input-group-addon">UTC</div>
//    </div>

  def validate(s: String): Either[String, ZonedDateTime] =
    try Right(ZonedDateTime.parse(s.replace(" ", "T") + "+00:00"))
    catch { case _: Exception =>
      Left("Please provide a date/time formatted like this: 2018-06-10 16:45")
    }

  override def inputType: String = "text"

  override def valueAsString(value: ZonedDateTime): String = value.toString
}
