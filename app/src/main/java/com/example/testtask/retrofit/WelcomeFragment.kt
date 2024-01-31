package com.example.testtask.retrofit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.testtask.LoginFragmentDirections
import com.example.testtask.R


class WelcomeFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_welcome, container, false)

        val phoneNumber = WelcomeFragmentArgs.fromBundle(requireArguments()).phoneNumber
        val token = WelcomeFragmentArgs.fromBundle(requireArguments()).token

        val textview = view.findViewById<TextView>(R.id.success_text)
        textview.text = "Здравствуйте," + "${phoneNumber}"+ ",\n Ваш токен " + "${token}"


        return view
    }


}