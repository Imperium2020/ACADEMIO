package com.imperium.academio.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.imperium.academio.R

class TemplateFragment : Fragment() {
    private var msg: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        msg = if (arguments != null) {
            requireArguments().getString(MSG)
        } else {
            "Hello World"
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.template_default, container, false)
        val t = v.findViewById<TextView>(R.id.tv_counter)
        t.text = msg
        return v
    }

    companion object {
        private const val MSG = "param1"
        fun newInstance(m: String?): TemplateFragment {
            val fragment = TemplateFragment()
            val args = Bundle()
            args.putString(MSG, m)
            fragment.arguments = args
            return fragment
        }
    }
}