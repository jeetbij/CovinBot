package com.example.cowin.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class CovinDataDTO {

    private List<Center> centers;

    public List<Center> getCenters() {
        return this.centers;
    }

    public void setCenters(List<Center> centers) {
        this.centers = centers;
    }

    public class Center {

        private String pincode;

        @SerializedName("name")
        private String centerName;

        @SerializedName("address")
        private String centerAddress;

        @SerializedName("fee_type")
        private String feeType;

        @SerializedName("long")
        private String longitude;

        @SerializedName("lat")
        private String latitude;

        @SerializedName("district_name")
        private String districtName;

        @SerializedName("block_name")
        private String blockName;

        @SerializedName("center_id")
        private String centerId;

        @SerializedName("state_name")
        private String stateName;

        private String from;

        private String to;

        private List<Session> sessions;

        public String getPincode() {
            return this.pincode;
        }

        public void setPincode(String pincode) {
            this.pincode = pincode;
        }

        public String getCenterName() {
            return this.centerName;
        }

        public void setCenterName(String centerName) {
            this.centerName = centerName;
        }

        public String getCenterAddress() {
            return this.centerAddress;
        }

        public void setCenterAddress(String centerAddress) {
            this.centerAddress = centerAddress;
        }

        public String getFeeType() {
            return this.feeType;
        }

        public void setFeeType(String feeType) {
            this.feeType = feeType;
        }

        public String getLongitude() {
            return this.longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public String getLatitude() {
            return this.latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getDistrictName() {
            return this.districtName;
        }

        public void setDistrictName(String districtName) {
            this.districtName = districtName;
        }

        public String getBlockName() {
            return this.blockName;
        }

        public void setBlockName(String blockName) {
            this.blockName = blockName;
        }

        public String getCenterId() {
            return this.centerId;
        }

        public void setCenterId(String centerId) {
            this.centerId = centerId;
        }

        public String getStateName() {
            return this.stateName;
        }

        public void setStateName(String stateName) {
            this.stateName = stateName;
        }

        public String getFrom() {
            return this.from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return this.to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public List<Session> getSessions() {
            return this.sessions;
        }

        public void setSessions(List<Session> sessions) {
            this.sessions = sessions;
        }

        public class Session {

            private String date;

            private String vaccine;

            private List<String> slots;

            @SerializedName("min_age_limit")
            private Integer minAgeLimit;

            @SerializedName("session_id")
            private String sessionId;

            @SerializedName("available_capacity")
            private Integer availableCapacity;

            public String getDate() {
                return this.date;
            }

            public void setDate(String date) {
                this.date = date;
            }

            public String getVaccine() {
                return this.vaccine;
            }

            public void setVaccine(String vaccine) {
                this.vaccine = vaccine;
            }

            public List<String> getSlots() {
                return this.slots;
            }

            public void setSlots(List<String> slots) {
                this.slots = slots;
            }

            public Integer getMinAgeLimit() {
                return this.minAgeLimit;
            }

            public void setMinAgeLimit(Integer minAgeLimit) {
                this.minAgeLimit = minAgeLimit;
            }

            public String getSessionId() {
                return this.sessionId;
            }

            public void setSessionId(String sessionId) {
                this.sessionId = sessionId;
            }

            public Integer getAvailableCapacity() {
                return this.availableCapacity;
            }

            public void setAvailableCapacity(Integer availableCapacity) {
                this.availableCapacity = availableCapacity;
            }

        }

    }

}
