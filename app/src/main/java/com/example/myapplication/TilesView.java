package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;


class Card {
    Paint p = new Paint();
    int outline = 10;

    public Card(int color) {
        this.color = color;
    }

    public void CardProperties(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    int color, backColor = Color.DKGRAY, solved = Color.WHITE;
    boolean isOpen = false; // цвет карты
    float x, y, width, height;

    public void draw(Canvas c) {
        // нарисовать карту в виде цветного прямоугольника
        if (isOpen) {
            p.setColor(color);
        } else p.setColor(backColor);
        c.drawRect(x+outline,y+outline, x+width - outline, y+height - outline, p);
    }
    public boolean flip (float touch_x, float touch_y) {
        if (touch_x >= x && touch_x <= x + width && touch_y >= y && touch_y <= y + height) {
            isOpen = ! isOpen;
            return true;
        } else return false;
    }

}

public class TilesView extends View {

    int row = 4, col = 4;

    // пауза для запоминания карт
    final int PAUSE_LENGTH = 2; // в секундах
    boolean isOnPauseNow = false;
    boolean won = false;
    // число открытых карт
    int openedCard = 0;

    Card[] openedCards = new Card[2];

    //ArrayList<Card> cards = new ArrayList<>();
    Card[][] cards = new Card[row][col];

    ArrayList<Integer> colors = new ArrayList<Integer>(Arrays.asList(
            Color.YELLOW, Color.GREEN,
            Color.BLUE, Color.BLACK,
            Color.MAGENTA, Color.CYAN,
            Color.RED, Color.rgb(255, 175, 175)
    ));



    public TilesView(Context context) {
        super(context);
    }

    public TilesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // 1) заполнить массив tiles случайными цветами
        // сгенерировать поле 2*n карт, при этом
        // должно быть ровно n пар карт разных цветов
        int current = 0;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                int color;
                if (current >= row * col / 2) {
                    int random = (int) (Math.random() * colors.size());
                    Log.d("random", "r: " + random);
                    color = colors.get(random);
                    colors.remove(random); // like movie task
                } else {
                    color = colors.get(current);
                }
                cards[i][j] = new Card(color);
                current++;

            }
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int t_height = canvas.getHeight() / row;
        int t_width = canvas.getWidth() / col; // like color tiles

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {

                int left = j * t_width;
                int top = i * t_height;

                cards[i][j].CardProperties(left, top, t_width, t_height);
                cards[i][j].draw(canvas);

            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 3) получить координаты касания
        int x = (int) event.getX();
        int y = (int) event.getY();
        // 4) определить тип события
        if (event.getAction() == MotionEvent.ACTION_DOWN && !isOnPauseNow)
        {
            // палец коснулся экрана

            for (int i = 0; i < row; i++) {
                for (int j = 0; j < col; j++) {

                    if (openedCard == 0) {
                        if (cards[i][j].flip(x, y)) {

                            openedCard++;
                            Log.d("mytag", "card flipped: " + openedCard + " - " + cards[i][j]);
                            openedCards[0] = cards[i][j];
                            invalidate();
                            return true;
                        }
                    }

                    if (openedCard == 1) {
                        // перевернуть карту с задержкой
                        if (cards[i][j].flip(x, y)) {
                            openedCard++;
                            // 4 если открылись карты одинакового цвета, удалить их из списка
                            //Log.d("mytag", "card flipped: " + openedCard + " - " + cards[i][j]);
                            openedCards[1] = cards[i][j];
                            Log.d("mytag", "card : " + " - " + openedCards[0].color);
                            Log.d("mytag", "card : " + " - " + openedCards[1].color);

                            if (openedCards[0].color == openedCards[1].color) {

                                openedCards[0].backColor = openedCards[0].color;
                                openedCards[1].backColor = openedCards[1].color;

                                Toast toast = Toast.makeText(getContext(), "Same card!", Toast.LENGTH_SHORT);
                                toast.show();

                                //"disappear"
                                openedCards[0].backColor = openedCards[0].solved;
                                openedCards[1].backColor = openedCards[1].solved;

                            }
                            invalidate();
                            PauseTask task = new PauseTask();
                            task.execute(PAUSE_LENGTH);
                            isOnPauseNow = true;

                            return true;
                        }
                    }

                }
            }

        }


        // заставляет экран перерисоваться
        return true;
    }

    public void newGame() {
        // запуск новой игры
    }

    class PauseTask extends AsyncTask<Integer, Void, Void> {
        @Override
        protected Void doInBackground(Integer... integers) {
            Log.d("mytag", "Pause started");
            try {
                Thread.sleep(integers[0] * 1000); // передаём число секунд ожидания
            } catch (InterruptedException e) {}
            Log.d("mytag", "Pause finished");
            return null;
        }

        // после паузы, перевернуть все карты обратно


        @Override
        protected void onPostExecute(Void aVoid) {
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < col; j++) {
                    if (cards[i][j].isOpen) {
                        cards[i][j].isOpen = false;
                    }
                }
            }
            openedCard = 0;
            isOnPauseNow = false;
            invalidate();
        }
    }
}
