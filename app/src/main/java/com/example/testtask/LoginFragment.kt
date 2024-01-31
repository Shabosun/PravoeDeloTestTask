package com.example.testtask

import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.example.testtask.databinding.FragmentLoginBinding
import com.example.testtask.retrofit.RetrofitApi
import com.example.testtask.retrofit.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException

class LoginFragment : Fragment() {

    private val AUTH = "Bearer Shabosun12"

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var retrofitApi: RetrofitApi
    private lateinit var gettedCode : String
    private lateinit var number : String
    private lateinit var token : String




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.buttonGetCode.setOnClickListener {
            if(binding.phoneEditText.text?.length != 12)
            {
                Toast.makeText(context,"Поле заполнено неверно" ,Toast.LENGTH_SHORT).show()
            }
            else{
                number = binding.phoneEditText.text.toString().replace('+', ' ')

                getCode(number)
            }

        }

        binding.buttonSendCode.setOnClickListener{

            if(binding.codeEditText.text.toString() == gettedCode)
            {
                getToken(number, gettedCode)

            }else
            {
                Toast.makeText(context, "Код неверный", Toast.LENGTH_SHORT).show()
                binding.requestCodeTextview.visibility = VISIBLE
            }
        }


        binding.requestCodeTextview.setOnClickListener{
            if(ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED)
            {
                //sendSMS(number, gettedCode)
                regenerateCode(number)
            }
            else{
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.SEND_SMS), 100)
            }


        }






        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        retrofitApi = RetrofitInstance.create(RetrofitApi::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    private fun showFieldForCode()
    {
        binding.apply {
            phoneEditText.visibility = INVISIBLE
            buttonGetCode.visibility = INVISIBLE
            textView.visibility = INVISIBLE


            buttonSendCode.visibility = VISIBLE
            codeEditText.visibility = VISIBLE
            requestCodeTextview.visibility = VISIBLE

        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


        if(requestCode == 100 && grantResults.count() > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            sendSMS(number, gettedCode)
        }
        else
        {
            Toast.makeText(context, "Разрешение отклонено",Toast.LENGTH_SHORT).show()
        }
    }

    private fun getCode(number: String) {

        try {
            CoroutineScope(Dispatchers.IO).launch {
                val response = retrofitApi.getCode(AUTH, number)
                lateinit var message: String


                if (response.isSuccessful) {
                    message = response.errorBody()?.string()?.let {
                        JSONObject(it).getString("message")
                    }.toString()

                    activity?.runOnUiThread {
                        val code = response.body()
                        if (code != null) {

                            gettedCode = code.code
                            if (code.status == "old") {
                                //по запросу
                                //binding.requestCodeTextview.visibility = VISIBLE
                                showFieldForCode()

                                Toast.makeText(context, "Если не помните код, нажмите 'запросить код' ", Toast.LENGTH_SHORT).show()


                            } else if (code.status == "new") {
                                sendSMS(number, gettedCode)
                                showFieldForCode()
                                //смс отправляется автоматически

                            }


                        }

                    }
                }
                else{


                    message = response.errorBody()?.string()?.let {
                        JSONObject(it).getString("message")
                    }.toString()

                    if(response.code() in 400..499 )
                    {
                        activity?.runOnUiThread{ binding.error.text = message }
                        //Log.d(LOG_TAG, "Error:" + errorBody?.string() + " Error code: " + response.code())
                    }
                    else if(response.code() >= 500)
                    {
                        activity?.runOnUiThread { binding.error.text= message}
                        //Log.d(LOG_TAG, "Error:" + errorBody?.string() + " Error code: " + response.code())
                    }
                }
            }




        } catch (e: IOException) {
            //Log.d(LOG_TAG, "Error: " + e.message)
        }

    }

    private fun getToken(phone_number : String, code : String){
        try{
            CoroutineScope(Dispatchers.IO).launch {

                val response = retrofitApi.getToken(AUTH, phone_number, gettedCode)
                lateinit var message : String

                if(response.isSuccessful)
                {
                    message = response.errorBody()?.string()?.let {
                        JSONObject(it).getString("message")
                    }.toString()
                    activity?.runOnUiThread{
                        val tkn = response.body()
                        if(tkn != null){

                            token = tkn
                            val action = LoginFragmentDirections.actionLoginFragmentToWelcomeFragment(number, token)
                            view?.findNavController()?.navigate(action)

                        }
                    }





                }else
                {
                    message = response.errorBody()?.string()?.let {
                        JSONObject(it).getString("message")
                    }.toString()


                    if(response.code() in 400..499 )
                    {
                        activity?.runOnUiThread{ binding.error.text = message }
                        //Log.d(LOG_TAG, "Error:" + errorBody?.string() + " Error code: " + response.code())
                    }
                    else if(response.code() >= 500)
                    {
                        activity?.runOnUiThread { binding.error.text= message}
                        //Log.d(LOG_TAG, "Error:" + errorBody?.string() + " Error code: " + response.code())
                    }
                }
            }

        }catch(e : IOException)
        {
            //Log.d(LOG_TAG, "Error: " + e.message)
        }

    }

    private fun sendSMS(phone : String, code : String)
    {
        try{
            val smsManager : SmsManager = SmsManager.getDefault()

            smsManager.sendTextMessage(phone, null,code, null, null )
            Toast.makeText(context, gettedCode, Toast.LENGTH_SHORT).show()
        }
        catch(e : Exception)
        {
            Toast.makeText(context,"не отправилось", Toast.LENGTH_SHORT).show()
        }



    }

    private fun regenerateCode(number : String)
    {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val response = retrofitApi.regenerateCode(AUTH, number)
                lateinit var message: String


                if (response.isSuccessful) {
                    message = response.errorBody()?.string()?.let {
                        JSONObject(it).getString("message")
                    }.toString()

                    activity?.runOnUiThread {
                        val new_code = response.body()
                        if (new_code != null) {

                            gettedCode = new_code
                            sendSMS(number, gettedCode)


                        }

                    }
                }
                else{


                    message = response.errorBody()?.string()?.let {
                        JSONObject(it).getString("message")
                    }.toString()

                    if(response.code() in 400..499 )
                    {
                        activity?.runOnUiThread{ binding.error.text = message }
                        //Log.d(LOG_TAG, "Error:" + errorBody?.string() + " Error code: " + response.code())
                    }
                    else if(response.code() >= 500)
                    {
                        activity?.runOnUiThread { binding.error.text= message}
                        //Log.d(LOG_TAG, "Error:" + errorBody?.string() + " Error code: " + response.code())
                    }
                }
            }




        } catch (e: IOException) {
            //Log.d(LOG_TAG, "Error: " + e.message)
        }
    }








}