package com.abrarshakhi.lumen.app

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

object LaunchIntents {

    fun notes(context: Context): Intent = intent(context, LaunchActions.OPEN_NOTES)

    fun newNote(context: Context): Intent =
        intent(context, LaunchActions.NEW_NOTE).setData("$SCHEME://note/new".toUri())

    fun note(context: Context, noteId: Long): Intent =
        intent(context, LaunchActions.OPEN_NOTE)
            .setData("$SCHEME://note/$noteId".toUri())
            .putExtra(LaunchActions.EXTRA_NOTE_ID, noteId)

    fun settings(context: Context): Intent = intent(context, LaunchActions.OPEN_SETTINGS)

    private fun intent(context: Context, action: String): Intent =
        Intent(context, MainActivity::class.java)
            .setAction(action)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

    private const val SCHEME = "lumen"
}
