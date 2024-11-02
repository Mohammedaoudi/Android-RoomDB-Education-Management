package ma.ensa.projet.adapters.admin.listener

import android.view.View

fun interface ItemClickListener {
    fun onClick(view: View?, position: Int, isLongClick: Boolean)
}
