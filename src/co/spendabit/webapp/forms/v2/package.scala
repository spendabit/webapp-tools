package co.spendabit.webapp.forms

import co.spendabit.webapp.forms.controls.Field

package object v2 {

  trait WebForm1[A] extends v2.BaseWebForm[A] {
    protected def fields: Field[A]
    protected def f1 = fields
    protected def fieldsSeq = Seq(f1)
    protected def seqToTuple(s: Seq[_]) = s.head.asInstanceOf[A]
    protected def widgetsHTML(values: Option[A]): Seq[xml.NodeSeq] = {
      val vs = values.map(vs => Some(vs)).getOrElse(None)
      Seq(f1.widgetHTML(vs))
    }
  }
}
