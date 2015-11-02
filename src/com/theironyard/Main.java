package com.theironyard;

import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;
import java.sql.*;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;


/*
ROWNUM does not work fully
Will address issue next day in class i.e. November 3rd, 2015
 */
public class Main {

    public static void main(String[] args) throws SQLException {

        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS beers (name VARCHAR, type VARCHAR)");

        Spark.get(
                "/",
                ((request, response) -> {
                    Session session = request.session();
                    ArrayList<Beer> beers = selectBeer(conn);
                    String username = session.attribute("username");
                    if (username == null) {
                        return new ModelAndView(new HashMap(), "not-logged-in.html");
                    }
                    HashMap m = new HashMap();
                    m.put("username", username);
                    m.put("beers", beers);
                    return new ModelAndView(m, "logged-in.html");
                }),
                new MustacheTemplateEngine()
        );//End of Spark.get() "/"

        Spark.post(
                "/login",
                ((request, response) -> {
                    String username = request.queryParams("username");
                    Session session = request.session();
                    session.attribute("username", username);
                    response.redirect("/");
                    return "";
                })
        );//End of Spark.post() "/login"

        Spark.post(
                "/create-beer",
                ((request, response) -> {
                    Beer beer = new Beer();
                    beer.name = request.queryParams("beername");
                    beer.type = request.queryParams("beertype");
                    insertBeer(conn, beer.name, beer.type);
                    response.redirect("/");
                    return "";
                })
        );//End of Spark.post() "/create-beer

        Spark.post(
                "/delete-beer",
                ((request, response) -> {
                    String id = request.queryParams("beerid");
                    try {
                        int idNum = Integer.valueOf(id);
                        deleteBeer(conn, idNum);
                    } catch (Exception e) {

                    }
                    response.redirect("/");
                    return "";
                })
        );//End of Spark.post() "/delete-beer"

        Spark.post(
                "/edit-beer",
                ((request, response) -> {
                    String editBeer = request.queryParams("beerid");
                    try {
                        PreparedStatement stmt2 = conn.prepareStatement("UPDATE beers SET name = ? AND type = ? WHERE ROWNUM = ?");
                        String beerName = request.queryParams("beername");
                        String beerType = request.queryParams("beertype");
                        int beerNum = Integer.valueOf(editBeer);
                        updateBeer(conn,beerName, beerType, beerNum );
                        stmt2.execute();
                    } catch (Exception e){

                    }
                    response.redirect("/");
                    return "";
                })
        );//End of Spark.post() "/edit-beer"

        conn.close();
    }//End of Main Method

    static void insertBeer(Connection conn, String beerName, String beerType) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO beers VALUES (?, ?)");
        stmt.setString(1, beerName);
        stmt.setString(2, beerType);
        stmt.execute();
    }//End of insertBeer Method

    static void deleteBeer(Connection conn, int selectNum) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE from players WHERE ROWNUM = ?");
        stmt.setInt(1, selectNum);
        stmt.execute();
    }//End of deleteBeer Method

    static ArrayList<Beer> selectBeer(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery("SELECT * FROM beers");
        ArrayList<Beer> beerArrayList = new ArrayList<>();
        int tempID = 1;
        while(results.next()){
            String beerName = results.getString("name");
            String beerType = results.getString("type");
            Beer beer = new Beer(tempID,beerName, beerType);
            beerArrayList.add(beer);
            tempID++;
        }
        return beerArrayList;
    }//End of selectBeer Method

    static void updateBeer(Connection conn, String beerName, String beerType, int selectNum) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE beers SET name = ? AND type = ? WHERE ROWNUM = ?");
        stmt.setString(1, beerName);
        stmt.setString(2, beerType);
        stmt.setInt(3, selectNum);
        stmt.execute();
    }//End of updateBeer Method

}//End of Main Class
