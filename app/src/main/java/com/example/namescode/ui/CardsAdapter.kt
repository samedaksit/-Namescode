package com.example.namescode.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.namescode.R
import com.example.namescode.model.Card

class CardsAdapter(
    private val cardList: ArrayList<Card>,
    var isNarrator: Boolean = false,
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
        private val imageHolder: CardView = itemView.findViewById(R.id.imageHolder)
        private val image: ImageView = itemView.findViewById(R.id.cardImageIV)
        private val imageLayout: ConstraintLayout = itemView.findViewById(R.id.imageLayout)

        fun bind(card: Card) {
            this.card = card
            word.text = card.cardBasic?.cardWord
            val context = itemView.context
            if (isNarrator) {
                card.cardColor?.let { cardView.setCardBackgroundColor(it) }
                if (card.cardShowColor == true) {
                    if (card.cardShowed == false) {
                        when (card.cardColor) {
                            Color.RED -> {
                                forRedCardsExtra(context)
                            }
                            Color.BLUE -> {
                                forBlueCardsExtra(context)
                            }
                            Color.BLACK -> {
                                forBlackCardsBasic(context)
                            }
                            Color.WHITE -> {
                                forWhiteCardsBasic(context)
                            }
                        }
                        card.cardShowed = true
                    } else {
                        if (card.isScaled == false) {
                            when (card.cardColor) {
                                Color.RED -> {
                                    forRedCardsBasic(context)
                                }
                                Color.BLUE -> {
                                    forBlueCardsBasic(context)
                                }
                                Color.BLACK -> {
                                    forBlackCardsBasic(context)
                                }
                                Color.WHITE -> {
                                    forWhiteCardsBasic(context)
                                }
                            }
                        } else {
                            scaleDownX()
                            when (card.cardColor) {
                                Color.RED -> {
                                    forRedCardsBasic(context)
                                }
                                Color.BLUE -> {
                                    forBlueCardsBasic(context)
                                }
                                Color.BLACK -> {
                                    forBlackCardsBasic(context)
                                }
                                Color.WHITE -> {
                                    forWhiteCardsBasic(context)
                                }
                            }
                        }
                    }
                }
            } else if (card.cardShowColor == true) {
                if (card.cardShowed == false) {
                    when (card.cardColor) {
                        Color.RED -> {
                            forRedCardsExtra(context)
                        }
                        Color.BLUE -> {
                            forBlueCardsExtra(context)
                        }
                        Color.BLACK -> {
                            forBlackCardsBasic(context)
                        }
                        Color.WHITE -> {
                            forWhiteCardsBasic(context)
                        }
                    }
                    card.cardShowed = true
                } else {
                    if (card.isScaled == false) {
                        when (card.cardColor) {
                            Color.RED -> {
                                forRedCardsBasic(context)
                            }
                            Color.BLUE -> {
                                forBlueCardsBasic(context)
                            }
                            Color.BLACK -> {
                                forBlackCardsBasic(context)
                            }
                            Color.WHITE -> {
                                forWhiteCardsBasic(context)
                            }
                        }
                    } else {
                        scaleDownX()
                        when (card.cardColor) {
                            Color.RED -> {
                                forRedCardsBasic(context)
                            }
                            Color.BLUE -> {
                                forBlueCardsBasic(context)
                            }
                            Color.BLACK -> {
                                forBlackCardsBasic(context)
                            }
                            Color.WHITE -> {
                                forWhiteCardsBasic(context)
                            }
                        }
                    }
                }
            }
        }

        private fun scaleDownX() {
            val scaleDownY = ObjectAnimator.ofFloat(imageHolder, "scaleY", 0.5f)
            scaleDownY.duration = 0
            val scaleDown = AnimatorSet()
            scaleDown.play(scaleDownY)
            scaleDownY.addUpdateListener {
                imageHolder.invalidate()
                imageHolder.pivotX = 0F
                imageHolder.pivotY = 0F
            }
            scaleDown.start()
        }

        private fun forRedCardsBasic(context: Context) {
            imageHolder.visibility = View.VISIBLE
            image.setImageResource(R.drawable.father)
            imageHolder.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.default_red_color
                )
            )
            cardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.default_red_color
                )
            )
        }

        private fun forRedCardsExtra(context: Context) {
            val fadeIn = ObjectAnimator.ofFloat(imageHolder, "alpha", 0f, 1f)
            fadeIn.doOnStart {
                imageHolder.visibility = View.VISIBLE
                image.setImageResource(R.drawable.father)
            }
            fadeIn.duration = 1000
            fadeIn.start()
            imageHolder.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.default_red_color
                )
            )
            cardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.default_red_color
                )
            )
        }

        private fun forBlueCardsBasic(context: Context) {
            imageHolder.visibility = View.VISIBLE
            image.setImageResource(R.drawable.cookie)
            imageHolder.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.default_blue_color
                )
            )
            cardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.default_blue_color
                )
            )
        }

        private fun forBlueCardsExtra(context: Context) {
            val valueAnimator = ValueAnimator.ofFloat(0F, 360F)
            valueAnimator.doOnStart {
                imageHolder.visibility = View.VISIBLE
                image.setImageResource(R.drawable.cookie)
            }
            valueAnimator.addUpdateListener {
                val value = it.animatedValue as Float
                imageHolder.rotation = value
            }
            valueAnimator.interpolator = LinearInterpolator()
            valueAnimator.duration = 500
            valueAnimator.start()
            imageHolder.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.default_blue_color
                )
            )
            cardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.default_blue_color
                )
            )
        }

        private fun forBlackCardsBasic(context: Context) {
            imageHolder.visibility = View.VISIBLE
            image.setImageResource(R.drawable.snow)
            imageHolder.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.black
                )
            )
            imageLayout.setBackgroundColor(Color.BLACK)
            cardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.black
                )
            )
        }

        private fun forWhiteCardsBasic(context: Context) {
            imageHolder.visibility = View.VISIBLE
            image.setImageResource(R.drawable.socks)
            imageHolder.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.white
                )
            )
            cardView.setCardBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.white
                )
            )
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                if (v != null) {
                    listener.onItemClick(position, cardView, imageHolder)
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
        fun onItemClick(position: Int, cardView: View, imageHolder: CardView)
    }
}