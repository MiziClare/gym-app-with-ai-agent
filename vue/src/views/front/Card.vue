<template>
    <div class="user-profile-container">
        <div class="card-container" v-if="user.id">
            <div class="user-info">
                <div class="member-card" ref="memberCard" :style="cardBackgroundStyle">
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
                        <!-- remain blank -->
                    </div>
                    <div class="card-footer">
                        <div class="user-name">{{ user.name }}</div>
                        <div class="username">@{{ user.username }}</div>
                    </div>
                </div>

                <div class="card-actions">
                    <el-button type="primary" @click="downloadCard" icon="el-icon-download">Download Card</el-button>
                    <el-button type="success" @click="sendCardByEmail" icon="el-icon-message">Send to Email</el-button>
                </div>

                <!-- Email sending dialog -->
                <el-dialog title="Send Membership Card" :visible.sync="emailDialogVisible" width="400px">
                    <el-form :model="emailForm" :rules="emailRules" ref="emailFormRef" label-width="100px">
                        <el-form-item label="Email" prop="email">
                            <el-input v-model="emailForm.email" placeholder="Enter email address"></el-input>
                        </el-form-item>
                    </el-form>
                    <div slot="footer" class="dialog-footer">
                        <el-button @click="emailDialogVisible = false">Cancel</el-button>
                        <el-button type="primary" @click="confirmSendEmail" :loading="sending">Send</el-button>
                    </div>
                </el-dialog>
            </div>

            <div class="pattern-selector">
                <h3>Card Style</h3>
                <div class="pattern-options">
                    <div v-for="(pattern, index) in backgroundPatterns" :key="index" class="pattern-option"
                        :class="{ 'active': selectedPattern === index }" @click="selectPattern(index)">
                        <div class="pattern-preview" :style="{ backgroundImage: pattern.css }"></div>
                        <div class="pattern-name">{{ pattern.name }}</div>
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
import html2canvas from 'html2canvas'

export default {
    name: "Card",
    data() {
        return {
            user: {},
            loading: true,
            qrCodeUrl: '',
            emailDialogVisible: false,
            sending: false,
            emailForm: {
                email: ''
            },
            emailRules: {
                email: [
                    { required: true, message: 'Please enter email address', trigger: 'blur' },
                    { type: 'email', message: 'Please enter a valid email address', trigger: 'blur' }
                ]
            },
            selectedPattern: 0,
            backgroundPatterns: [
                {
                    name: 'Classic Gold',
                    css: 'linear-gradient(135deg, #ffeaa7 0%, #f7d794 100%)'
                },
                {
                    name: 'Geometric',
                    css: 'linear-gradient(45deg, #f7d794 25%, #ffeaa7 25%, #ffeaa7 50%, #f7d794 50%, #f7d794 75%, #ffeaa7 75%, #ffeaa7 100%)',
                    backgroundSize: '40px 40px'
                },
                {
                    name: 'Waves',
                    css: 'linear-gradient(135deg, #ffeaa7 25%, #f7d794 25%, #f7d794 50%, #ffeaa7 50%, #ffeaa7 75%, #f7d794 75%, #f7d794 100%)'
                },
                {
                    name: 'Circles',
                    css: 'radial-gradient(circle at 50% 50%, #ffeaa7 0%, #ffeaa7 50%, #f7d794 100%)'
                },
                {
                    name: 'Luxury',
                    css: 'linear-gradient(to right, #ffeaa7, #f7d794, #ffd700, #f7d794, #ffeaa7)'
                },
                {
                    name: 'Dots',
                    css: 'linear-gradient(90deg, #ffeaa7 0%, #ffeaa7 90%, #f7d794 90%, #f7d794 100%), linear-gradient(0deg, #ffeaa7 0%, #ffeaa7 90%, #f7d794 90%, #f7d794 100%)',
                    backgroundSize: '20px 20px'
                },
                {
                    name: 'Stripes',
                    css: 'linear-gradient(90deg, #ffeaa7, #ffeaa7 20px, #f7d794 20px, #f7d794 40px)'
                }
            ]
        }
    },
    computed: {
        cardBackgroundStyle() {
            const pattern = this.backgroundPatterns[this.selectedPattern];
            const style = {
                backgroundImage: pattern.css
            };

            // Add other possible background properties
            if (pattern.backgroundSize) {
                style.backgroundSize = pattern.backgroundSize;
            }

            if (pattern.backgroundPosition) {
                style.backgroundPosition = pattern.backgroundPosition;
            }

            return style;
        }
    },
    created() {
        // Get current user information
        this.loadCurrentUser();
    },
    methods: {
        selectPattern(index) {
            this.selectedPattern = index;
            // Save user preference to localStorage
            localStorage.setItem('card-pattern', index);
        },
        loadCurrentUser() {
            this.loading = true;
            // Get current user information from local storage
            const currentUser = JSON.parse(localStorage.getItem('xm-user') || '{}');

            // Load saved pattern preference
            const savedPattern = localStorage.getItem('card-pattern');
            if (savedPattern !== null) {
                this.selectedPattern = parseInt(savedPattern);
            }

            if (!currentUser.username) {
                this.loading = false;
                return;
            }

            this.$request.get('/user/selectAll', {
                params: { username: currentUser.username }
            }).then(res => {
                if (res.code === '200' && res.data && res.data.length > 0) {
                    this.user = res.data[0];
                    // Generate QR code after user data is loaded
                    this.generateQRCode(this.user.username);
                    // Pre-fill email with user's email
                    this.emailForm.email = this.user.email || '';
                }
                this.loading = false;
            }).catch(() => {
                this.loading = false;
            });
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
        },
        async captureCard() {
            try {
                const card = this.$refs.memberCard;
                if (!card) return null;

                // 创建一个克隆元素，应用当前样式
                const clone = card.cloneNode(true);
                clone.style.position = 'absolute';
                clone.style.left = '-9999px';
                clone.style.width = card.offsetWidth + 'px';
                clone.style.height = card.offsetHeight + 'px';

                // 应用当前背景样式
                const pattern = this.backgroundPatterns[this.selectedPattern];
                clone.style.backgroundImage = pattern.css;
                if (pattern.backgroundSize) {
                    clone.style.backgroundSize = pattern.backgroundSize;
                }
                if (pattern.backgroundPosition) {
                    clone.style.backgroundPosition = pattern.backgroundPosition;
                }

                // 添加到文档中以便捕获
                document.body.appendChild(clone);

                // 等待一小段时间确保背景完全渲染
                await new Promise(resolve => setTimeout(resolve, 100));

                // 使用html2canvas捕获卡片
                const canvas = await html2canvas(clone, {
                    useCORS: true,
                    scale: 2, // 提高分辨率
                    backgroundColor: '#ffeaa7', // 使用默认背景色，避免透明导致的问题
                    logging: false,
                    removeContainer: true
                });

                // 移除克隆元素
                document.body.removeChild(clone);

                return canvas.toDataURL('image/png');
            } catch (error) {
                console.error('Error capturing card:', error);
                return null;
            }
        },
        async downloadCard() {
            try {
                const dataUrl = await this.captureCard();
                if (!dataUrl) {
                    this.$message.error('Failed to generate card image');
                    return;
                }

                // 创建下载链接
                const link = document.createElement('a');
                link.href = dataUrl;
                link.download = `membership_card_${this.user.username}.png`;
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);

                this.$message.success('Card downloaded successfully');
            } catch (error) {
                console.error('Error downloading card:', error);
                this.$message.error('Failed to download card');
            }
        },
        sendCardByEmail() {
            this.emailDialogVisible = true;
        },
        async confirmSendEmail() {
            this.$refs.emailFormRef.validate(async (valid) => {
                if (!valid) return;

                try {
                    this.sending = true;

                    // Capture the card image
                    const dataUrl = await this.captureCard();
                    if (!dataUrl) {
                        this.sending = false;
                        return;
                    }

                    // Get the current user's token
                    const currentUser = JSON.parse(localStorage.getItem('xm-user') || '{}');
                    const token = currentUser.token;

                    // Send the image to the backend
                    await this.$request({
                        url: '/user/sendCardByEmail',
                        method: 'POST',
                        headers: {
                            'token': token
                        },
                        data: {
                            email: this.emailForm.email,
                            cardImage: dataUrl,
                            userId: this.user.id
                        }
                    }).then(response => {
                        if (response.code === '200') {
                            this.$message.success('Membership card sent to your email successfully');
                            this.emailDialogVisible = false;
                        } else {
                            this.$message.error(response.msg || 'Failed to send email');
                        }
                    }).catch(error => {
                        console.error('Error sending email:', error);
                        // If the backend has processed the request but the frontend did not receive the response, still show success
                        this.$message({
                            message: 'Email may have been sent, please check your inbox',
                            type: 'warning'
                        });
                        this.emailDialogVisible = false;
                    });
                } catch (error) {
                    console.error('Error sending email:', error);
                    // If the backend has processed the request but the frontend did not receive the response, still show success
                    this.$message({
                        message: 'Email may have been sent, please check your inbox',
                        type: 'warning'
                    });
                    this.emailDialogVisible = false;
                } finally {
                    this.sending = false;
                }
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
    position: relative;
}

.card-container {
    display: flex;
    justify-content: center;
    width: 100%;
}

.user-info {
    display: flex;
    flex-direction: column;
    align-items: center;
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
    transition: background-image 0.3s;
}

.card-actions {
    margin-top: 30px;
    display: flex;
    gap: 20px;
    justify-content: center;
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
    margin: 0 0 10px 0;
    color: #333;
    font-weight: bold;
    font-family: 'Dangrek', sans-serif;
    font-size: 40px;
}

.card-body {
    flex: 1;
    /* remain blank */
}

.card-footer {
    margin-top: auto;
    border-top: 2px dashed #e0c989;
    padding-top: 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.user-name {
    font-size: 32px;
    font-weight: bold;
    color: #333;
    margin-bottom: 5px;
    font-family: 'Montserrat', sans-serif;
}

.username {
    font-size: 18px;
    color: #666;
    font-style: italic;
    font-family: 'Roboto', sans-serif;
}

.member-id {
    font-size: 18px;
    color: #666;
    font-family: 'Roboto', sans-serif;
    font-weight: bold;
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

.pattern-selector {
    width: 180px;
    margin-bottom: 20px;
    margin-top: 30px;
    background-color: #f9f9f9;
    padding: 15px;
    border-radius: 10px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
    position: absolute;
    right: 110px;
    top: 50px;
}

.pattern-selector h3 {
    margin-bottom: 15px;
    color: #333;
    text-align: center;
    font-size: 18px;
    border-bottom: 1px solid #eee;
    padding-bottom: 10px;
    font-family: 'Inter', sans-serif;
}

.pattern-options {
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.pattern-option {
    cursor: pointer;
    text-align: center;
    transition: all 0.3s;
    display: flex;
    align-items: center;
    padding: 8px;
    border-radius: 6px;
    background-color: #fff;
    font-family: 'Inter', sans-serif;
}

.pattern-option.active {
    transform: translateX(5px);
    background-color: #ecf5ff;
}

.pattern-option.active .pattern-preview {
    border: 3px solid #409EFF;
}

.pattern-preview {
    width: 50px;
    height: 30px;
    border-radius: 8px;
    margin-right: 10px;
    border: 1px solid #ddd;
    transition: all 0.3s;
}

.pattern-name {
    font-size: 12px;
    color: #666;
    flex: 1;
    text-align: left;
}

@media (max-width: 1200px) {
    .card-container {
        padding-right: 200px;
    }

    .pattern-selector {
        width: 180px;
        right: 10px;
    }
}

@media (max-width: 992px) {
    .card-container {
        padding-right: 0;
    }

    .pattern-selector {
        position: static;
        width: 800px;
        margin-top: 20px;
    }

    .pattern-options {
        flex-direction: row;
        flex-wrap: wrap;
        gap: 10px;
        justify-content: center;
    }

    .pattern-option {
        width: 120px;
    }
}
</style>
