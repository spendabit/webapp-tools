package co.spendabit.webapp.forms

package object v3 {

  trait WebForm1[A] extends v3.BaseWebForm[A] {
    protected def fields: Field[A]
    protected def f1 = fields
    protected def fieldsSeq = Seq(f1)
    protected def seqToTuple(s: Seq[_]) = s.head.asInstanceOf[A]
    protected def widgetsHTML(values: Option[A]): Seq[xml.NodeSeq] = {
      val vs = values.flatMap(vs => Some(vs))
      Seq(f1.control.html(vs))
    }
  }
}
