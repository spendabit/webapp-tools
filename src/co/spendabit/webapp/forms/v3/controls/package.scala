package co.spendabit.webapp.forms.v3

import org.apache.commons.fileupload.FileItem

package object controls {

  sealed abstract class Control[T]/*(val defaultValue: Option[T])*/ {
    def html(value: Option[T] = None): xml.NodeSeq
  }

  abstract class TextBasedInput[T] extends Control[T] {
    def validate(s: String): Either[String, T]
  }

  abstract class FileBasedInput[T] extends Control[T] {
    def validate(s: Option[FileItem]): Either[String, T]
  }
}
