package com.example.layoutfinal.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.layoutfinal.R
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class DashboardFragment : Fragment() {
    private lateinit var textView: TextView
    private lateinit var ratingView: TextView
    private lateinit var seekBar1: SeekBar
    private lateinit var seekBar2: SeekBar
    private lateinit var seekBar3: SeekBar
    private lateinit var seekBar4: SeekBar
    private val ipPc = "192.168.86.22"
    private val port = 12345

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        textView = view.findViewById(R.id.textView)
        ratingView = view.findViewById(R.id.ratingView)
        seekBar1 = view.findViewById(R.id.seekBar1)
        seekBar2 = view.findViewById(R.id.seekBar2)
        seekBar3 = view.findViewById(R.id.seekBar3)
        seekBar4 = view.findViewById(R.id.seekBar4)
        val button1 = view.findViewById<Button>(R.id.button1)

        // Set SeekBar listeners
        seekBar1.setOnSeekBarChangeListener(seekBarListener("Rating 1: "))
        seekBar2.setOnSeekBarChangeListener(seekBarListener("Rating 2: "))
        seekBar3.setOnSeekBarChangeListener(seekBarListener("Rating 3: "))
        seekBar4.setOnSeekBarChangeListener(seekBarListener("Rating 4: "))

        // Button Click Listener
        button1.setOnClickListener {
            textView.text = "Clicked button !"
            val message = "Hola PC desde Android"
            sendMessageToServer(ipPc, port, message)
        }
    }

    private fun seekBarListener(ratingText: String): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                ratingView.text = "$ratingText $progress"
                sendMessageToServer(ipPc, port,"$ratingText $progress")
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        }
    }

    private fun sendMessageToServer(serverIP: String, port: Int, message: String) {
        Thread {
            try {
                //SEND
                val socket = Socket(serverIP, port)
                val out = PrintWriter(socket.getOutputStream(), true)
                out.println(message) // 🔥 Send the custom message
                Log.d("SocketConnection", "Attempting to connect to $serverIP:$port")

                //RECIEVE
                val inputStream = socket.getInputStream()
                val buffer = ByteArray(1024)
                val bytesRead = inputStream.read(buffer)
                val response = String(buffer, 0, bytesRead)
                Log.d("ServerResponse", "Response: $response")

                // Update UI
                requireActivity().runOnUiThread {
                    if (response != null) {
                        textView.text = "Server Response: $response"
                    } else {
                        textView.text = "No response from server."
                    }
                }

                socket.close()
            } catch (e: IOException) {
                Log.e("SocketError", "Error: ${e.message}", e)
            }
        }.start()
    }
}
