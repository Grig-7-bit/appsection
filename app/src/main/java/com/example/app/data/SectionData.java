package com.example.app.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Patterns;

import java.util.ArrayList;
import java.util.List;

public class SectionData implements Parcelable {
    private List<String> registeredUsers;
    private String id = "";
    private String title = "";
    private long timestamp = 0L;
    private String description = "";
    private String category = "";
    private String city = "";
    private String location = "";
    private List<String> scheduleDay = new ArrayList<>();
    private List<String> scheduleTime = new ArrayList<>();
    private String price = "";
    private int maxParticipants = 0;
    public int currentParticipants = 0;
    private String ownerId = "";
    private String contactPhone = "";
    private String contactEmail = "";
    private List<String> imageUrls = new ArrayList<>();
    private String coach = ""; // Новое поле для тренера

    public SectionData() {}

    protected SectionData(Parcel in) {
        id = in.readString();
        title = in.readString();
        timestamp = in.readLong();
        description = in.readString();
        category = in.readString();
        city = in.readString();
        location = in.readString();
        scheduleDay = in.createStringArrayList();
        scheduleTime = in.createStringArrayList();
        price = in.readString();
        maxParticipants = in.readInt();
        currentParticipants = in.readInt();
        ownerId = in.readString();
        contactPhone = in.readString();
        contactEmail = in.readString();
        imageUrls = in.createStringArrayList();
        coach = in.readString(); // Чтение нового поля из Parcel
    }

    public static final Creator<SectionData> CREATOR = new Creator<SectionData>() {
        @Override
        public SectionData createFromParcel(Parcel in) {
            return new SectionData(in);
        }

        @Override
        public SectionData[] newArray(int size) {
            return new SectionData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeLong(timestamp);
        dest.writeString(description);
        dest.writeString(category);
        dest.writeString(city);
        dest.writeString(location);
        dest.writeStringList(scheduleDay);
        dest.writeStringList(scheduleTime);
        dest.writeString(price);
        dest.writeInt(maxParticipants);
        dest.writeInt(currentParticipants);
        dest.writeString(ownerId);
        dest.writeString(contactPhone);
        dest.writeString(contactEmail);
        dest.writeStringList(imageUrls);
        dest.writeString(coach); // Запись нового поля в Parcel
    }

    // Геттер для поля coach
    public String getCoach() {
        return coach;
    }

    // Сеттер для поля coach
    public void setCoach(String coach) {
        this.coach = coach != null ? coach : "";
    }

    // Остальные методы класса остаются без изменений
    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (TextUtils.isEmpty(id)) {
            throw new IllegalArgumentException("ID cannot be empty");
        }
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (TextUtils.isEmpty(title)) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        this.title = title;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Invalid timestamp");
        }
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (TextUtils.isEmpty(description)) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        if (TextUtils.isEmpty(category)) {
            throw new IllegalArgumentException("Category cannot be empty");
        }
        this.category = category;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        if (TextUtils.isEmpty(city)) {
            throw new IllegalArgumentException("City cannot be empty");
        }
        this.city = city;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        if (TextUtils.isEmpty(location)) {
            throw new IllegalArgumentException("Location cannot be empty");
        }
        this.location = location;
    }

    public List<String> getScheduleDay() {
        return new ArrayList<>(scheduleDay);
    }

    public void setScheduleDay(List<String> scheduleDay) {
        if (scheduleDay == null || scheduleDay.isEmpty()) {
            throw new IllegalArgumentException("Schedule days cannot be empty");
        }
        this.scheduleDay = new ArrayList<>(scheduleDay);
    }

    public List<String> getScheduleTime() {
        return new ArrayList<>(scheduleTime);
    }

    public void setScheduleTime(List<String> scheduleTime) {
        if (scheduleTime == null || scheduleTime.isEmpty()) {
            throw new IllegalArgumentException("Schedule times cannot be empty");
        }
        this.scheduleTime = new ArrayList<>(scheduleTime);
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price != null ? price : "";
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        if (maxParticipants <= 0) {
            throw new IllegalArgumentException("Max participants must be positive");
        }
        this.maxParticipants = maxParticipants;
    }

    public int getCurrentParticipants() {
        return currentParticipants;
    }

    public void setCurrentParticipants(int currentParticipants) {
        if (currentParticipants < 0) {
            throw new IllegalArgumentException("Current participants cannot be negative");
        }
        // Убрана проверка на превышение maxParticipants для корректной десериализации
        this.currentParticipants = currentParticipants;
    }

    // Добавляем метод для безопасного увеличения участников
    public void safelyIncrementParticipants() {
        if (currentParticipants < maxParticipants) {
            currentParticipants++;
        }
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        if (TextUtils.isEmpty(ownerId)) {
            throw new IllegalArgumentException("Owner ID cannot be empty");
        }
        this.ownerId = ownerId;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        if (TextUtils.isEmpty(contactPhone)) {
            throw new IllegalArgumentException("Phone cannot be empty");
        }
        if (!Patterns.PHONE.matcher(contactPhone).matches()) {
            throw new IllegalArgumentException("Invalid phone format");
        }
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        if (TextUtils.isEmpty(contactEmail)) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(contactEmail).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.contactEmail = contactEmail;
    }

    public List<String> getImageUrls() {
        return imageUrls != null ? new ArrayList<>(imageUrls) : new ArrayList<>();
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? new ArrayList<>(imageUrls) : new ArrayList<>();
    }

    public void addImageUrl(String imageUrl) {
        if (imageUrl == null) return;
        if (imageUrls == null) {
            imageUrls = new ArrayList<>();
        }
        imageUrls.add(imageUrl);
    }

    public void removeImageUrl(String imageUrl) {
        if (imageUrl != null && imageUrls != null) {
            imageUrls.remove(imageUrl);
        }
    }

    public boolean validateSchedules() {
        if (scheduleDay == null || scheduleTime == null || scheduleDay.size() != scheduleTime.size()) {
            throw new IllegalStateException("Days and times count must match");
        }
        return true;
    }

    public List<String> getRegisteredUsers() {
        return registeredUsers != null ? registeredUsers : new ArrayList<>();
    }

    public void setRegisteredUsers(List<String> registeredUsers) {
        this.registeredUsers = registeredUsers != null ? new ArrayList<>(registeredUsers) : new ArrayList<>();
    }
}