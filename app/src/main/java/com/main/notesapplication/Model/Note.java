package com.main.notesapplication.Model;
import android.os.Parcel;
import android.os.Parcelable;


public class Note implements Parcelable {
    private String date;
    private String title;
    private String body;
    private String imageId;
    private String noteNumber;
    private double lat,lon;
    public Note() {

    }

    public Note(String title, String body, String imageId) {
        super();
        this.title = title;
        this.body = body;
        this.imageId = imageId;
    }
    public static final Parcelable.Creator<Note> CREATOR
            = new Parcelable.Creator<Note>() {
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    protected Note(Parcel in) {
        title = in.readString();
        body = in.readString();
        imageId = in.readString();
        date=in.readString();
        noteNumber=in.readString();
        lat=in.readDouble();
        lon=in.readDouble();
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(body);
        parcel.writeString(imageId);
        parcel.writeString(date);
        parcel.writeString(noteNumber);
        parcel.writeDouble(lat);
        parcel.writeDouble(lon);
    }


    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public String getNoteNumber() {
        return noteNumber;
    }

    public void setNoteNumber(String noteNumber) {
        this.noteNumber = noteNumber;
    }

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
}
