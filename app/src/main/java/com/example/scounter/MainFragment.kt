package com.example.scounter

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import android.util.Log

import java.util.*

class MainFragment : Fragment() {

    private var stepCount = 0
    private var isCounterRunning = false

    private lateinit var stepTextView: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var resetButton: ImageButton

    private val handler = Handler(Looper.getMainLooper())
    private var timerTask: TimerTask? = null
    private val timer = Timer()

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        stepTextView = view.findViewById(R.id.stepTextView)
        startButton = view.findViewById(R.id.startButton)
        stopButton = view.findViewById(R.id.stopButton)
        resetButton = view.findViewById(R.id.resetButton)

        loadStepCount()

        startButton.setOnClickListener {
            startVirtualSensor()
            showToast("Rennen beginnt!")
        }

        stopButton.setOnClickListener {
            stopVirtualSensor()
            showToast("Aufzeichnung gestoppt")
        }

        resetButton.setOnClickListener {
            showResetConfirmation()
        }

        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.motivation_sound)

        return view
    }

    private fun startVirtualSensor() {
        if (!isCounterRunning) {
            isCounterRunning = true
            scheduleNextStep()
        }
    }

    private fun stopVirtualSensor() {
        if (isCounterRunning) {
            timerTask?.cancel()
            isCounterRunning = false
            saveStepCount()
        }
    }

    private fun scheduleNextStep() {
        timerTask = object : TimerTask() {
            override fun run() {
                handler.post {
                    simulateStepChange()
                    if (isCounterRunning) {
                        scheduleNextStep()
                    }
                }
            }
        }
        timer.schedule(timerTask, 500)
    }

    private fun simulateStepChange() {
        stepCount++
        stepTextView.text = stepCount.toString()

        if (stepCount % 10 == 0) {
            showToast("$stepCount Schritte erreicht!")
            playSound()
        }
    }


    private fun playSound() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }

        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.motivation_sound)

        mediaPlayer?.setOnCompletionListener {
            it.release()
            mediaPlayer = null
        }

        try {
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Fehler beim Abspielen des Sounds.")
        }
    }



    private fun showResetConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Zurücksetzen")
            .setMessage("Möchten Sie die Schrittzählung zurücksetzen?")
            .setPositiveButton("Ja") { _, _ -> resetStepCount() }
            .setNegativeButton("Nein", null)
            .show()
    }

    private fun resetStepCount() {
        stepCount = 0
        stepTextView.text = stepCount.toString()
        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("stepCount")
        editor.apply()
    }

    private fun saveStepCount() {
        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("stepCount", stepCount)
        editor.apply()
    }

    private fun loadStepCount() {
        val sharedPreferences = requireActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        stepCount = sharedPreferences.getInt("stepCount", 0)
        stepTextView.text = stepCount.toString()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}
