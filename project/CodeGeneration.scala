import sbt.File

object CodeGeneration {

  case class GeneratedScalaFile(path: File, content: String)

  val generatedFiles: Seq[GeneratedScalaFile] = webForms :+ versionFile

  private def versionFile =
    GeneratedScalaFile(new File("co/spendabit/version.scala"),
      content =
        s"""
          |package co.spendabit
          |object version { def get = "${build.libVersion}" }""".stripMargin.trim)

  // XXX: Surely this can be better done with Scala's macros, however, I still need to learn
  // XXX: how to use those... :P
  private def webForms = Range.inclusive(1, 22).flatMap { n =>

    val typeParamNames = Range.inclusive('A', 'Z').map(_.toChar).take(n)
    assert(typeParamNames.length == n)
    val typeParamsStr = typeParamNames.mkString(", ")
    val oneToN = Range.inclusive(1, n)

    val content = s"""
      |package co.spendabit.webapp.forms.v2
      |
      |import co.spendabit.webapp.forms.controls.Field
      |
      |trait WebForm$n[$typeParamsStr] extends BaseWebForm[($typeParamsStr)] {
      |  protected def fields: (${ typeParamNames.map(t => s"Field[$t]").mkString(", ") })
      |  protected def fieldsSeq = Seq(${ Range.inclusive(1, n).map("fields._" + _).mkString(", ") })
      |  protected def seqToTuple(s: Seq[_]) =
      |   (${ Range.inclusive(0, n - 1).map("s(" + _ + ")").mkString(", ") }).
      |     asInstanceOf[($typeParamsStr)]
      |  protected def widgetsHTML(values: Option[($typeParamsStr)]): Seq[xml.NodeSeq] = {
      |    val vs = values.map(vs =>
      |      (${ oneToN.map("Some(vs._" + _ + ")").mkString(", ") })).
      |        getOrElse((${ oneToN.map(_ => "None").mkString(", ") }))
      |    Seq(${ oneToN.map(x => s"fields._$x.widgetHTML(vs._$x)").mkString(", ")})
      |  }
      |}
    """.stripMargin
    val v2f = GeneratedScalaFile(new File(s"co/spendabit/webapp/forms/v2/WebForm$n.scala"), content)

    val v3f = GeneratedScalaFile(
      new File(s"co/spendabit/webapp/forms/v3/WebForm$n.scala"),
      genCodeForV3(n))

    if (n == 1) Seq(v3f) else Seq(v2f, v3f)
  }

  private def genCodeForV3(n: Int) = {

    val typeParamNames = Range.inclusive('A', 'Z').map(_.toChar).take(n)
    assert(typeParamNames.length == n)
    val typeParamsStr = typeParamNames.mkString(", ")
    val oneToN = Range.inclusive(1, n)

    s"""
      |package co.spendabit.webapp.forms.v3
      |
      |import scala.language.higherKinds
      |
      |case class WebForm$n[FieldT[X] <: Field[X], $typeParamsStr](${ typeParamNames.map(t => s"field$t: FieldT[$t]").mkString(", ") }) extends BaseWebForm[FieldT, ($typeParamsStr)] {
      |  protected def fieldsSeq = Seq(${ typeParamNames.map("field" + _).mkString(", ") })
      |  protected def seqToTuple(s: Seq[_]) =
      |   (${ Range.inclusive(0, n - 1).map("s(" + _ + ")").mkString(", ") }).
      |     asInstanceOf[($typeParamsStr)]
      |  protected def widgetsHTML(values: Option[($typeParamsStr)]): Seq[xml.NodeSeq] = {
      |    val vs = values.map(vs =>
      |      (${ if (n == 1) "Some(vs)" else oneToN.map("Some(vs._" + _ + ")").mkString(", ") })).
      |        getOrElse((${ oneToN.map(_ => "None").mkString(", ") }))
      |    Seq(${ typeParamNames.zip(oneToN).map { case (f, i) => if (n == 1) s"field$f.control.html(vs)" else s"field$f.control.html(vs._$i)" }.mkString(", ")})
      |  }
      |}
    """.stripMargin
  }
}
