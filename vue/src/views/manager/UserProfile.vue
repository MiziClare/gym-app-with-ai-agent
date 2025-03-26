<template>
    <div class="user-profile-container">
        <div class="user-header">
            <el-page-header @back="goBack" :content="'🪪'"></el-page-header>
        </div>

        <div class="user-info" v-if="user.id">
            <div class="member-card">
                <div class="qr-code">
                    <img :src="qrCodeUrl" alt="QR Code">
                </div>
                <div class="card-header">
                    <div class="avatar">
                        <img v-if="user.avatar" :src="user.avatar" alt="User Avatar">
                        <div v-else class="avatar-placeholder">
                            <i class="el-icon-user-solid"></i>
                        </div>
                    </div>
                    <div class="card-title">
                        <h2>GYM &thinsp;MEMBERSHIP &thinsp;CARD</h2>
                        <div class="member-id">ID: {{ user.id }}</div>
                    </div>
                </div>
                <div class="card-body">
                    <div class="info-row">
                        <div class="info-label">Username:</div>
                        <div class="info-value">{{ user.username }}</div>
                    </div>
                    <div class="info-row">
                        <div class="info-label">Name:</div>
                        <div class="info-value">{{ user.name }}</div>
                    </div>
                    <div class="info-row">
                        <div class="info-label">Phone:</div>
                        <div class="info-value">{{ user.phone }}</div>
                    </div>
                    <div class="info-row">
                        <div class="info-label">Email:</div>
                        <div class="info-value">{{ user.email }}</div>
                    </div>
                    <div class="info-row">
                        <div class="info-label">Balance:</div>
                        <div class="info-value">￡ {{ user.account.toFixed(2) }}</div>
                    </div>
                </div>
            </div>
        </div>

        <div v-else class="loading-container">
            <el-empty description="Loading user information..." v-if="loading"></el-empty>
            <el-empty description="User information not found" v-else></el-empty>
        </div>
    </div>
</template>

<script>
import QRCode from 'qrcode'

export default {
    name: "UserProfile",
    data() {
        return {
            user: {},
            loading: true,
            qrCodeUrl: ''
        }
    },
    created() {
        // Get username from route parameters
        const username = this.$route.params.username;
        if (username) {
            this.loadUserByUsername(username);
        } else {
            this.loading = false;
        }
    },
    methods: {
        loadUserByUsername(username) {
            this.loading = true;
            this.$request.get('/user/selectAll', {
                params: { username: username }
            }).then(res => {
                if (res.code === '200' && res.data && res.data.length > 0) {
                    this.user = res.data[0];
                    // Generate QR code after user data is loaded
                    this.generateQRCode(this.user.username);
                }
                this.loading = false;
            }).catch(() => {
                this.loading = false;
            });
        },
        goBack() {
            this.$router.push('/user');
        },
        generateQRCode(username) {
            // Create the specific user URL
            const baseUrl = window.location.origin;
            const url = `${baseUrl}/user/${username}`;

            // Generate QR code
            QRCode.toDataURL(url, {
                width: 100,
                margin: 1,
                color: {
                    dark: '#333333',
                    light: '#ffffff'
                }
            })
                .then(url => {
                    this.qrCodeUrl = url;
                })
                .catch(err => {
                    console.error('Error generating QR code:', err);
                });
        }
    }
}
</script>

<style scoped>
.user-profile-container {
    padding: 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.user-header {
    margin-bottom: 20px;
    width: 100%;
}

.user-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 100%;
}

.member-card {
    background-color: #ffeaa7;
    width: 800px;
    height: 500px;
    border-radius: 20px;
    box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
    padding: 30px;
    display: flex;
    flex-direction: column;
    position: relative;
    overflow: hidden;
    margin-top: 50px;
}

.qr-code {
    position: absolute;
    top: 40px;
    right: 50px;
    width: 100px;
    height: 100px;
    background-color: white;
    border-radius: 10px;
    padding: 5px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    display: flex;
    justify-content: center;
    align-items: center;
}

.qr-code img {
    width: 100%;
    height: 100%;
    object-fit: contain;
}

.card-header {
    display: flex;
    align-items: center;
    margin-bottom: 40px;
}

.avatar {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    overflow: hidden;
    margin-right: 30px;
    background-color: #fff;
    display: flex;
    justify-content: center;
    align-items: center;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.avatar img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.avatar-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: #f0f0f0;
}

.avatar-placeholder i {
    font-size: 60px;
    color: #ccc;
}

.card-title {
    flex: 1;
}

.card-title h2 {
    font-size: 28px;
    margin: 0 0 10px 0;
    color: #333;
    font-weight: bold;
    font-family: 'Dangrek', sans-serif;
    font-size: 40px;
}

.member-id {
    font-size: 18px;
    color: #666;
    font-family: 'Roboto', sans-serif;
    font-weight: bold;
}

.card-body {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    font-family: 'Montserrat', sans-serif;
}

.info-row {
    display: flex;
    margin-bottom: 20px;
    border-bottom: 1px dashed #e0c989;
    padding-bottom: 10px;
}

.info-label {
    width: 120px;
    font-weight: bold;
    color: #333;
}

.info-value {
    flex: 1;
    color: #555;
}

.loading-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 300px;
}
</style>
