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
  private def webForms = Range.inclusive(2, 22).flatMap { n =>

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

    val content2 = s"""
      |package co.spendabit.webapp.forms.v3
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
      |    Seq(${ oneToN.map(x => s"fields._$x.control.html(vs._$x)").mkString(", ")})
      |  }
      |}
    """.stripMargin
    val v3f = GeneratedScalaFile(new File(s"co/spendabit/webapp/forms/v3/WebForm$n.scala"), content2)

    Seq(v2f, v3f)
  }
}
