package com.chteuchteu.lesjoiesdusysadmin;

public class Gif {
	public String nom = "";
	public String urlArticle = "";
	public String urlGif = "";
	public String date = "";
	public int state = 0;
	
	public static int ST_UNKNOWN = 0;
	public static int ST_EMPTY = 1;
	public static int ST_DOWNLOADING = 2;
	public static int ST_COMPLETE = 3;
	
	public Gif() { }
	
	public boolean isValide() {
		if (!nom.equals("") && !urlGif.equals(""))
			return true;
		return false;
	}
	
	public boolean equals(Gif g) {
		if (!this.nom.equals(g.nom))
			return false;
		if (!this.urlArticle.equals("") && !g.urlArticle.equals("") && !this.urlArticle.equals(g.urlArticle))
			return false;
		if (!this.urlGif.equals("") && !g.urlGif.equals("") && !this.urlGif.equals(g.urlGif))
			return false;
		if (!this.date.equals("") && !g.date.equals("") && !this.date.equals(g.date))
			return false;
		return true;
	}
}