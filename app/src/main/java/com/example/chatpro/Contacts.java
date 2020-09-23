package com.example.chatpro;

public class Contacts
{
    public String name, images, status;

    public Contacts()
    {

    }

    public Contacts(String name, String images, String status) {
        this.name = name;
        this.images = images;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
