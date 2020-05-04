package com.example.android29;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;

import com.example.android29.chess.Chess;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.CountDownTimer;
import android.os.Handler;
import android.text.InputType;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.example.android29.chess.Chess.getValue;

public class ChessMatchActivity extends AppCompatActivity {

    public static boolean isPlayback = false;
    View view1;
    View view2;

    boolean start = true;
    boolean isWhiteTurn = true;
    boolean checkFlag = true;
    boolean draw = false;
    boolean justUndo;
    RecordedMatches.MatchNode match = new RecordedMatches.MatchNode();
    RecordedMatches.MatchNode playBackMatch = new RecordedMatches.MatchNode();

    int currentMoveIndex;

    Chess chess;

    String move = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess_match);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //activates the up arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecordedMatches.MatchNode match = new RecordedMatches.MatchNode();

        setChessBoard();

        androidx.gridlayout.widget.GridLayout board = findViewById(R.id.gridLayout);

        //Handle Back Button


        Log.i("state:", "running");

        if(isPlayback){
            currentMoveIndex = -1;
            Button resignButton = (Button) findViewById(R.id.resignButton);
            resignButton.setEnabled(false);
            resignButton.setVisibility(View.GONE);

            Button undoButton = (Button) findViewById(R.id.undoButton);
            undoButton.setEnabled(false);
            undoButton.setVisibility(View.GONE);

            Button drawButton = (Button) findViewById(R.id.drawButton);
            drawButton.setEnabled(false);
            drawButton.setVisibility(View.GONE);

            Button aiButton = (Button) findViewById(R.id.aiButton);
            aiButton.setEnabled(false);
            aiButton.setVisibility(View.GONE);

            Intent in = getIntent();
            Bundle b = in.getExtras();
            playBackMatch.setTitle(b.getString("title"));
            playBackMatch.setMoves(b.getStringArrayList("moves"));
            playBackMatch.setWinner(b.getString("winner"));
            Date date = new Date(b.getLong("date"));
            playBackMatch.setDate(date);
        }
        else{
            Button nextMoveButton = (Button) findViewById(R.id.nextButton);
            nextMoveButton.setEnabled(false);
            nextMoveButton.setVisibility(View.GONE);

            Button prevMoveButton = (Button) findViewById(R.id.prevButton);
            prevMoveButton.setEnabled(false);
            prevMoveButton.setVisibility(View.GONE);
        }

    }

    public void tapped(View view){
        if(isPlayback){
            return;
        }
        else{
            Log.i("Position", view.getTag().toString() );

            if(start){
                view1 = view;
                move += view.getTag().toString()+" ";
                start = false;
            } else {
                //dest view
                view2 = view;

                move += view.getTag().toString();
            }


            if(view1 != null && view2 != null) {
                String moveResult = this.chess.start(move, isWhiteTurn);

                Toast.makeText(this, moveResult, Toast.LENGTH_SHORT).show();

                if(moveResult.equals("invalid")){
                    reset();
                    return;
                }

                if(moveResult.equals("Checkmate")){
                    Toast.makeText(this, moveResult, Toast.LENGTH_SHORT).show();
                    System.out.println("Running timer");

                    new CountDownTimer(3000,1000){

                        @Override
                        public void onTick(long millisUntilFinished) {
                        }

                        @Override
                        public void onFinish() {
                            System.out.println("Finished countdown");
                            saveGame();
                            return;
                        }
                    }.start();
                }

                Log.i("Moving: ", move);
                match.addMove(move);
                match.printMoves();

                ImageView startimg = (ImageView) view1;
                ImageView destimg = (ImageView) view2;

                //swapping images
                destimg.setImageDrawable(startimg.getDrawable());
                startimg.setImageResource(android.R.color.transparent);

                justUndo = false;

                reset();
                isWhiteTurn = !isWhiteTurn;

                if(draw){
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);
                    alert.setTitle("Would you like to draw?");

                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveGame();
                        }
                    });

                    alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            draw = false;
                        }
                    });

                    alert.show();

                }

            }
        }


    }


    public void reset(){
        view1 = null;
        view2 = null;
        start = true;
        move = "";
    }

    public void resignButtonPressed(View view){

        if(isWhiteTurn){
            Toast.makeText(this, "Black Wins", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "White Wins", Toast.LENGTH_SHORT).show();
        }
        saveGame();
    }

    public void saveGame(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        String winner = isWhiteTurn ? "Black Wins! " : "White Wins! ";

        if(draw){
            winner = "Draw!";
        }

        alert.setTitle( winner + "If you would like to save your match, enter a title.");

        final EditText gameTitle = new EditText(this);
        gameTitle.setInputType(InputType.TYPE_CLASS_TEXT);
        alert.setView(gameTitle);

        alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = gameTitle.getText().toString().trim();
                if(title.equals("")){
                    Toast.makeText(getApplicationContext(), "Must add a title!", Toast.LENGTH_SHORT).show();
                    saveGame();
                }else{
                    System.out.println("GAME TITLE: " + title);
                    match.setTitle(title);
                    match.setWinner(isWhiteTurn?"Black" : "White");
                    Date date = Calendar.getInstance().getTime();
                    match.setDate(date);
                    try {
                        RecordedMatches.recordedMatchesList.addMatch(match);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setChessBoard();
                    isWhiteTurn = true;
                    reset();
                    match = new RecordedMatches.MatchNode();
                }
            }
        });

        alert.setNegativeButton("Don't Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.out.println("Not saving game");
                setChessBoard();
                isWhiteTurn = true;
                reset();
                match = new RecordedMatches.MatchNode();
            }
        });

        alert.show();
    }

    public void undoButtonPressed(View view){

       // Toast.makeText(this, "Undo Button Pressed", Toast.LENGTH_SHORT).show();

        if(justUndo == true){
            Toast.makeText(this, "Can only undo last move!", Toast.LENGTH_SHORT).show();
        }
        else{
            if(match.getMoves().size() == 0){
                Toast.makeText(this, "No moves to undo!", Toast.LENGTH_SHORT).show();
            }else{
                ArrayList<String> movesArray = match.getMoves();
                String moveToUndo = movesArray.get(movesArray.size()-1);
                String[] oldAndNew = moveToUndo.split(" ");

                androidx.gridlayout.widget.GridLayout parentGrid = findViewById(R.id.gridLayout);
                ImageView from = (ImageView) parentGrid.findViewWithTag(oldAndNew[1]);
                ImageView to = (ImageView) parentGrid.findViewWithTag(oldAndNew[0]);
                //Swapping images

                to.setImageDrawable(from.getDrawable());
                //from.setImageResource(android.R.color.transparent);

                ImageView putKilledPieceBack = (ImageView)parentGrid.findViewWithTag(oldAndNew[1]);
                putKilledPieceBack.setImageResource(chess.getLastKilled());


                isWhiteTurn = !isWhiteTurn;
                chess.undoMove(moveToUndo);
                match.undoMove();
                match.printMoves();
                justUndo = true;
            }
        }

    }

    public void drawButtonPressed(View view){

        Toast.makeText(this, "Draw Button Pressed", Toast.LENGTH_SHORT).show();
        draw = !(draw);
        justUndo = false;

    }
    public void aiButtonPressed(View view){

        String AImove = generateMove();
        String moveResult = chess.start(AImove, isWhiteTurn);

        while(moveResult.equals("invalid")){
            AImove = generateMove();
            moveResult = chess.start(AImove, isWhiteTurn);
        }



        Toast.makeText(this, AImove+" "+moveResult, Toast.LENGTH_SHORT).show();

        if(moveResult.equals("Checkmate")){
            saveGame();
        }

        if(moveResult.equals("check")){
            checkFlag = true;
        }

        Log.i("Moving: ", move);
        match.addMove(AImove);


        String[] tags = AImove.split(" ");

        System.out.println(AImove);

        //finding views by tag
        androidx.gridlayout.widget.GridLayout parentGrid = findViewById(R.id.gridLayout);
        view1 = parentGrid.findViewWithTag(tags[0]);
        view2 = parentGrid.findViewWithTag(tags[1]);

        ImageView startimg = (ImageView) view1;
        ImageView destimg = (ImageView) view2;

        //swapping images
        destimg.setImageDrawable(startimg.getDrawable());
        startimg.setImageResource(android.R.color.transparent);

        justUndo = false;
        reset();
        isWhiteTurn = !isWhiteTurn;

    }

    public String generateMove(){
        String AImove = this.chess.makeAImove(isWhiteTurn);

        while(AImove.equals("noMoves")){
            AImove = this.chess.makeAImove(isWhiteTurn);
        }

        return AImove;
    }

    public void nextMovePressed(View view){
        if(currentMoveIndex+1 < playBackMatch.getMoves().size()){
            String move = playBackMatch.getMoves().get(currentMoveIndex+1);
            System.out.println("MOVE FROM PLAYBACK: " + move);

            this.chess.movePlayBack(move);

            String[] moves = move.split(" ");
            String oldPosition = moves[0];
            String newPosition = moves[1];

            androidx.gridlayout.widget.GridLayout parentGrid = findViewById(R.id.gridLayout);
            ImageView from = (ImageView) parentGrid.findViewWithTag(oldPosition);
            ImageView to = (ImageView) parentGrid.findViewWithTag(newPosition);
            //Swapping images
            to.setImageDrawable(from.getDrawable());
            from.setImageResource(android.R.color.transparent);

            currentMoveIndex=currentMoveIndex+1;

        }
        else{
            Toast.makeText(this, playBackMatch.getWinner() + " Wins!", Toast.LENGTH_SHORT).show();
        }
    }

    public void prevMovePressed(View view){
        //Toast.makeText(this, "Prev Move Button Pressed", Toast.LENGTH_SHORT).show();
        if(currentMoveIndex >=0){
            String move = playBackMatch.getMoves().get(currentMoveIndex);
            System.out.println("MOVE FROM PLAYBACK: " + move);

            this.chess.movePlayBack(move);

            String[] moves = move.split(" ");
            String oldPosition = moves[0];
            String newPosition = moves[1];

            androidx.gridlayout.widget.GridLayout parentGrid = findViewById(R.id.gridLayout);
            ImageView from = (ImageView) parentGrid.findViewWithTag(newPosition);
            ImageView to = (ImageView) parentGrid.findViewWithTag(oldPosition);
            //Swapping images
            to.setImageDrawable(from.getDrawable());
            from.setImageResource(android.R.color.transparent);

            currentMoveIndex=currentMoveIndex-1;

        }
        else{
            Toast.makeText(this, "No more moves!", Toast.LENGTH_SHORT).show();
        }

    }

    //RESET BACK BUTTON
    @Override
    public Intent getParentActivityIntent(){
        return getParentActivityIntentImpl();
    }

    @Override
    public Intent getSupportParentActivityIntent(){
        return getParentActivityIntentImpl();
    }

    private Intent getParentActivityIntentImpl(){
        Intent i = null;

        if(isPlayback){
            i = new Intent(this, RecordingsActivity.class);
        }else{
            i = new Intent(this, MainActivity.class);
        }
        return i;
    }

    public void setChessBoard(){
        //set chess board - black pieces
        justUndo = false;
        isWhiteTurn = true;
        chess = new Chess();
        ImageView blackRook1 = (ImageView) findViewById(R.id.a8ImageView);
        blackRook1.setImageResource(R.drawable.br);

        ImageView blackRook2 = (ImageView) findViewById(R.id.h8ImageView);
        blackRook2.setImageResource(R.drawable.br);

        ImageView blackKnight1 = (ImageView) findViewById(R.id.b8ImageView);
        blackKnight1.setImageResource(R.drawable.bn);

        ImageView blackKnight2 = (ImageView) findViewById(R.id.g8ImageView);
        blackKnight2.setImageResource(R.drawable.bn);

        ImageView blackBishop1 = (ImageView) findViewById(R.id.c8ImageView);
        blackBishop1.setImageResource(R.drawable.bb);

        ImageView blackBishop2 = (ImageView) findViewById(R.id.f8ImageView);
        blackBishop2.setImageResource(R.drawable.bb);

        ImageView blackKing = (ImageView) findViewById(R.id.e8ImageView);
        blackKing.setImageResource(R.drawable.bk);

        ImageView blackQueen = (ImageView) findViewById(R.id.d8ImageView);
        blackQueen.setImageResource(R.drawable.bq);

        ImageView blackPawn1 = (ImageView) findViewById(R.id.a7ImageView);
        blackPawn1.setImageResource(R.drawable.bp);

        ImageView blackPawn2 = (ImageView) findViewById(R.id.b7ImageView);
        blackPawn2.setImageResource(R.drawable.bp);

        ImageView blackPawn3 = (ImageView) findViewById(R.id.c7ImageView);
        blackPawn3.setImageResource(R.drawable.bp);

        ImageView blackPawn4 = (ImageView) findViewById(R.id.d7ImageView);
        blackPawn4.setImageResource(R.drawable.bp);

        ImageView blackPawn5 = (ImageView) findViewById(R.id.e7ImageView);
        blackPawn5.setImageResource(R.drawable.bp);

        ImageView blackPawn6 = (ImageView) findViewById(R.id.f7ImageView);
        blackPawn6.setImageResource(R.drawable.bp);

        ImageView blackPawn7 = (ImageView) findViewById(R.id.g7ImageView);
        blackPawn7.setImageResource(R.drawable.bp);

        ImageView blackPawn8 = (ImageView) findViewById(R.id.h7ImageView);
        blackPawn8.setImageResource(R.drawable.bp);

        //set chess board - white pieces

        ImageView whiteRook1 = (ImageView) findViewById(R.id.a1ImageView);
        whiteRook1.setImageResource(R.drawable.wr);

        ImageView whiteRook2 = (ImageView) findViewById(R.id.h1ImageView);
        whiteRook2.setImageResource(R.drawable.wr);

        ImageView whiteKnight1 = (ImageView) findViewById(R.id.b1ImageView);
        whiteKnight1.setImageResource(R.drawable.wn);

        ImageView whiteKnight2 = (ImageView) findViewById(R.id.g1ImageView);
        whiteKnight2.setImageResource(R.drawable.wn);

        ImageView whiteBishop1 = (ImageView) findViewById(R.id.c1ImageView);
        whiteBishop1.setImageResource(R.drawable.wb);

        ImageView whiteBishop2 = (ImageView) findViewById(R.id.f1ImageView);
        whiteBishop2.setImageResource(R.drawable.wb);

        ImageView whiteKing = (ImageView) findViewById(R.id.e1ImageView);
        whiteKing.setImageResource(R.drawable.wk);

        ImageView whiteQueen = (ImageView) findViewById(R.id.d1ImageView);
        whiteQueen.setImageResource(R.drawable.wq);

        ImageView whitePawn1 = (ImageView) findViewById(R.id.a2ImageView);
        whitePawn1.setImageResource(R.drawable.wp);

        ImageView whitePawn2 = (ImageView) findViewById(R.id.b2ImageView);
        whitePawn2.setImageResource(R.drawable.wp);

        ImageView whitePawn3 = (ImageView) findViewById(R.id.c2ImageView);
        whitePawn3.setImageResource(R.drawable.wp);

        ImageView whitePawn4 = (ImageView) findViewById(R.id.d2ImageView);
        whitePawn4.setImageResource(R.drawable.wp);

        ImageView whitePawn5 = (ImageView) findViewById(R.id.e2ImageView);
        whitePawn5.setImageResource(R.drawable.wp);

        ImageView whitePawn6 = (ImageView) findViewById(R.id.f2ImageView);
        whitePawn6.setImageResource(R.drawable.wp);

        ImageView whitePawn7 = (ImageView) findViewById(R.id.g2ImageView);
        whitePawn7.setImageResource(R.drawable.wp);

        ImageView whitePawn8 = (ImageView) findViewById(R.id.h2ImageView);
        whitePawn8.setImageResource(R.drawable.wp);

        //Set empty other squares

        ImageView empty1 = (ImageView) findViewById(R.id.a3ImageView);
        empty1.setImageResource(android.R.color.transparent);

        ImageView empty2 = (ImageView) findViewById(R.id.b3ImageView);
        empty2.setImageResource(android.R.color.transparent);

        ImageView empty3 = (ImageView) findViewById(R.id.c3ImageView);
        empty3.setImageResource(android.R.color.transparent);

        ImageView empty4 = (ImageView) findViewById(R.id.d3ImageView);
        empty4.setImageResource(android.R.color.transparent);

        ImageView empty5 = (ImageView) findViewById(R.id.e3ImageView);
        empty5.setImageResource(android.R.color.transparent);

        ImageView empty6 = (ImageView) findViewById(R.id.f3ImageView);
        empty6.setImageResource(android.R.color.transparent);

        ImageView empty7 = (ImageView) findViewById(R.id.g3ImageView);
        empty7.setImageResource(android.R.color.transparent);

        ImageView empty8 = (ImageView) findViewById(R.id.h3ImageView);
        empty8.setImageResource(android.R.color.transparent);

        ImageView empty9 = (ImageView) findViewById(R.id.a4ImageView);
        empty9.setImageResource(android.R.color.transparent);

        ImageView empty10 = (ImageView) findViewById(R.id.b4ImageView);
        empty10.setImageResource(android.R.color.transparent);

        ImageView empty11 = (ImageView) findViewById(R.id.c4ImageView);
        empty11.setImageResource(android.R.color.transparent);

        ImageView empty12 = (ImageView) findViewById(R.id.d4ImageView);
        empty12.setImageResource(android.R.color.transparent);

        ImageView empty13 = (ImageView) findViewById(R.id.e4ImageView);
        empty13.setImageResource(android.R.color.transparent);

        ImageView empty14 = (ImageView) findViewById(R.id.f4ImageView);
        empty14.setImageResource(android.R.color.transparent);

        ImageView empty15 = (ImageView) findViewById(R.id.g4ImageView);
        empty15.setImageResource(android.R.color.transparent);

        ImageView empty16 = (ImageView) findViewById(R.id.h4ImageView);
        empty16.setImageResource(android.R.color.transparent);

        ImageView empty17 = (ImageView) findViewById(R.id.a5ImageView);
        empty17.setImageResource(android.R.color.transparent);

        ImageView empty18 = (ImageView) findViewById(R.id.b5ImageView);
        empty18.setImageResource(android.R.color.transparent);

        ImageView empty19 = (ImageView) findViewById(R.id.c5ImageView);
        empty19.setImageResource(android.R.color.transparent);

        ImageView empty20 = (ImageView) findViewById(R.id.d5ImageView);
        empty20.setImageResource(android.R.color.transparent);

        ImageView empty21 = (ImageView) findViewById(R.id.e5ImageView);
        empty21.setImageResource(android.R.color.transparent);

        ImageView empty22 = (ImageView) findViewById(R.id.f5ImageView);
        empty22.setImageResource(android.R.color.transparent);

        ImageView empty23= (ImageView) findViewById(R.id.g5ImageView);
        empty23.setImageResource(android.R.color.transparent);

        ImageView empty24 = (ImageView) findViewById(R.id.h5ImageView);
        empty24.setImageResource(android.R.color.transparent);

        ImageView empty25 = (ImageView) findViewById(R.id.a6ImageView);
        empty25.setImageResource(android.R.color.transparent);

        ImageView empty26 = (ImageView) findViewById(R.id.b6ImageView);
        empty26.setImageResource(android.R.color.transparent);

        ImageView empty27 = (ImageView) findViewById(R.id.c6ImageView);
        empty27.setImageResource(android.R.color.transparent);

        ImageView empty28 = (ImageView) findViewById(R.id.d6ImageView);
        empty28.setImageResource(android.R.color.transparent);

        ImageView empty29 = (ImageView) findViewById(R.id.e6ImageView);
        empty29.setImageResource(android.R.color.transparent);

        ImageView empty30 = (ImageView) findViewById(R.id.f6ImageView);
        empty30.setImageResource(android.R.color.transparent);

        ImageView empty31 = (ImageView) findViewById(R.id.g6ImageView);
        empty31.setImageResource(android.R.color.transparent);

        ImageView empty32 = (ImageView) findViewById(R.id.h6ImageView);
        empty32.setImageResource(android.R.color.transparent);
    }


}
