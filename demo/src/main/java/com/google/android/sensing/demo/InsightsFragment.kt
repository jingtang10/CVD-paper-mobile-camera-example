/*
 * Copyright 2022-2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.sensing.demo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


/** Fragment for the component list. */
class InsightsFragment : Fragment(R.layout.insights_fragment) {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    view.findViewById<Button>(R.id.button_restart).setOnClickListener {
      findNavController().navigate(R.id.action_insightsFragment_to_instructionFragment)
    }

    val insights = arrayOf(
      Insight("Tuberculosis", "87% likely"),
      Insight("Lung infection", "87% likely"),
      Insight("[category]", "[x]% likely"),
      Insight("[category]", "[x]% likely"),
      Insight("[category]", "[x]% likely"),
    )
    val recyclerView = view.findViewById<RecyclerView>(R.id.insights_list)
    recyclerView.layoutManager = LinearLayoutManager(this.context)
   recyclerView.adapter = InsightAdapter(insights)
  }
}

data class Insight(val cause: String, val likelihood: String)

class InsightAdapter(private val insights: Array<Insight>):
  RecyclerView.Adapter<InsightAdapter.ViewHolder>() {

  class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val cause: TextView = view.findViewById(R.id.cause)
    val likelihood: TextView = view.findViewById(R.id.likelihood)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.insight, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val insight = insights[position]
    holder.cause.text = insight.cause
    holder.likelihood.text = insight.likelihood
  }

  override fun getItemCount() = insights.size
}

