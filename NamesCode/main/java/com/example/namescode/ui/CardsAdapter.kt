package com.example.namescode.ui

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.namescode.R
import com.example.namescode.model.Card

class CardsAdapter(
    private val cardList: List<Card>,
    val isNarrator: Boolean = false,
    val listener: OnItemClickListener
) : RecyclerView.Adapter<CardsAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : View.OnClickListener,
        RecyclerView.ViewHolder(itemView) {
        private lateinit var card: Card

        init {
            itemView.setOnClickListener(this)
        }

        private val word: TextView = itemView.findViewById(R.id.cardText)
        private val cardView: CardView = itemView.findViewById(R.id.cardCardView)
        private val checkedImage: ImageView = itemView.findViewById(R.id.checkImage)

        fun bind(card: Card) {
            this.card = card
            word.text = card.cardBasic?.cardWord
            val context = itemView.context
            if (isNarrator) {
                card.cardColor?.let { cardView.setCardBackgroundColor(it) }
                if (card.cardShowColor == true) {
                    if(card.cardColor==Color.BLUE){
                        checkedImage.setColorFilter(Color.WHITE)
                    }
                    checkedImage.visibility = View.VISIBLE
                }
            } else if (card.cardShowColor == true) {
                when(card.cardColor){
                    Color.RED->{
                        val fadeOut = ObjectAnimator.ofFloat(cardView, "alpha", 1f, 0f)
                        fadeOut.duration = 1000
                        fadeOut.start()
                        val fadeIn = ObjectAnimator.ofFloat(cardView, "alpha", 0f, 1f)
                        fadeIn.duration = 1000
                        fadeIn.start()
                        cardView.setCardBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.default_red_color
                            )
                        )
                    }
                    Color.BLUE->{
                        val valueAnimator = ValueAnimator.ofFloat(0F, 360F)
                        valueAnimator.addUpdateListener {
                            val value = it.animatedValue as Float
                            cardView.rotation = value
                        }
                        valueAnimator.interpolator = LinearInterpolator()
                        valueAnimator.duration = 500
                        valueAnimator.start()
                        cardView.setCardBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.default_blue_color
                            )
                        )
                    }
                    Color.BLACK->{
                        cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.black))
                    }
                    Color.WHITE->{
                        cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
                    }
                }
            }
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                if (v != null) {
                    listener.onItemClick(position, cardView)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(cardList[position])
    }

    override fun getItemCount() = cardList.size

    interface OnItemClickListener {
        fun onItemClick(position: Int, cardView: View)
    }
}