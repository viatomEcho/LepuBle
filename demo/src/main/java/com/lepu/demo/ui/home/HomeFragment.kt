package com.lepu.demo.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.lepu.blepro.objs.Bluetooth
import com.lepu.demo.R
import com.lepu.demo.ui.scan.ScanActivity

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        val textView: TextView = root.findViewById(R.id.text_home)
        val button: Button = root.findViewById(R.id.o2ring)
        val er1: Button = root.findViewById(R.id.er1)
        val multiply: Button = root.findViewById(R.id.multiply)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        homeViewModel.button.observe(viewLifecycleOwner, Observer {
            button.text = it
        })

        button.setOnClickListener( View.OnClickListener {
            Intent(context, ScanActivity::class.java).apply {
                this.putExtra("curType", Bluetooth.MODEL_O2RING)
            }.also { intent -> context?.startActivity(intent) }
        })

        er1.setOnClickListener( View.OnClickListener {
            Intent(context, ScanActivity::class.java).apply {
                this.putExtra("curType", Bluetooth.MODEL_ER1)
            }.also { intent -> context?.startActivity(intent) }
        })

        multiply.setOnClickListener( View.OnClickListener {
            Intent(context, ScanActivity::class.java).apply {
                this.putExtra("curType", 33)
            }.also { intent -> context?.startActivity(intent) }
        })

        return root
    }
}