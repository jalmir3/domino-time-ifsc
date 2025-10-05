const DominoTime = {
    addButtonLoading: function(button, text = 'Processando...') {
        const btnText = button.querySelector('#btnText') || button;
        if (!button.dataset.originalText) {
            button.dataset.originalText = btnText.innerHTML;
        }
        button.classList.add('btn-loading');
        button.disabled = true;
        btnText.innerHTML = text;
    },

    removeButtonLoading: function(button) {
        const btnText = button.querySelector('#btnText') || button;
        button.classList.remove('btn-loading');
        button.disabled = false;
        btnText.innerHTML = button.dataset.originalText || 'Enviar';
    },

    autoRemoveAlert: function(alertId, delay = 2500) {
        const alert = document.getElementById(alertId);
        if (alert) {
            setTimeout(() => {
                alert.classList.add('fade');
                setTimeout(() => alert.remove(), 500);
            }, delay);
        }
    },

    initCopyButton: function() {
        const copyBtn = document.getElementById('copyAccessCodeBtn');
        const accessCodeInput = document.getElementById('accessCodeInput');

        if (copyBtn && accessCodeInput) {
            copyBtn.addEventListener('click', function() {
                accessCodeInput.select();
                accessCodeInput.setSelectionRange(0, 99999);

                try {
                    document.execCommand('copy');
                    const originalHtml = copyBtn.innerHTML;
                    copyBtn.innerHTML = '<i class="fas fa-check"></i>';
                    copyBtn.classList.add('copied');

                    setTimeout(() => {
                        copyBtn.innerHTML = originalHtml;
                        copyBtn.classList.remove('copied');
                    }, 2000);
                } catch (err) {
                    console.error('Erro ao copiar código:', err);
                }
            });
        }
    },

    initUserDropdown: function() {
        const userIcon = document.getElementById('userAvatarIcon');
        const dropdownContent = document.querySelector('.user-dropdown .dropdown-content');

        if (userIcon && dropdownContent) {
            userIcon.addEventListener('click', function(e) {
                e.stopPropagation();
                dropdownContent.classList.toggle('show');
            });

            document.addEventListener('click', function(e) {
                if (!e.target.closest('.user-dropdown')) {
                    dropdownContent.classList.remove('show');
                }
            });
        }
    }
};

const LoginPage = {
    init: function() {
        const loginForm = document.getElementById('loginForm');
        if (loginForm) {
            loginForm.addEventListener('submit', this.handleSubmit);
        }
    },

    onLoginSuccess: function() {
        fetch('/api/user/current')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Erro ao obter dados do usuário');
                }
                return response.json();
            })
            .then(user => {
                if (user.avatar) {
                    localStorage.setItem('userAvatar', user.avatar);
                }
                window.location.href = '/home';
            })
            .catch(error => {
                console.error('Erro:', error);
                window.location.href = '/home';
            });
    },

    handleSubmit: function(e) {
        const submitBtn = document.getElementById('submitBtn');
        const btnText = document.getElementById('btnText');

        DominoTime.addButtonLoading(submitBtn, 'Processando...');

        let checkLoginInterval = setInterval(function() {
            if (window.location.pathname !== '/login') {
                clearInterval(checkLoginInterval);
                if (window.location.pathname === '/home') {
                    LoginPage.onLoginSuccess();
                }
            }
        }, 100);

        setTimeout(() => {
            clearInterval(checkLoginInterval);
            if (window.location.pathname === '/login') {
                DominoTime.removeButtonLoading(submitBtn);
            }
        }, 10000);
    }
};

const RegistrationPage = {
    init: function() {
        const registerForm = document.getElementById('registerForm');
        if (registerForm) {
            registerForm.addEventListener('submit', this.handleSubmit);
        }

        DominoTime.autoRemoveAlert('errorAlert', 2500);
        DominoTime.autoRemoveAlert('successAlert', 5000);
    },

    handleSubmit: function(e) {
        const submitBtn = document.getElementById('submitBtn');
        DominoTime.addButtonLoading(submitBtn, 'Processando...');

        setTimeout(() => {
            DominoTime.removeButtonLoading(submitBtn);
        }, 10000);
    }
};

const ResetPasswordPage = {
    init: function() {
        const form = document.getElementById('resetPasswordForm');
        const submitBtn = document.getElementById('submitBtn');
        const btnText = document.getElementById('btnText');

        if (form && submitBtn) {
            form.addEventListener('submit', function() {
                DominoTime.addButtonLoading(submitBtn, 'Processando...');
            });
        }

        DominoTime.autoRemoveAlert('newPasswordErrorAlert', 2500);
        DominoTime.autoRemoveAlert('confirmPasswordErrorAlert', 2500);
    }
};

const ForgotPasswordPage = {
    init: function() {
        const form = document.getElementById('forgotPasswordForm');
        const submitBtn = document.getElementById('submitBtn');

        if (form && submitBtn) {
            form.addEventListener('submit', function() {
                DominoTime.addButtonLoading(submitBtn, 'Enviando...');
            });
        }

        DominoTime.autoRemoveAlert('successAlert', 5000);
        DominoTime.autoRemoveAlert('errorAlert', 3000);
    }
};

const JoinGamePage = {
    init: function() {
        const joinForm = document.getElementById('joinGameForm');
        if (joinForm) {
            joinForm.addEventListener('submit', this.handleSubmit);
        }

        DominoTime.autoRemoveAlert('errorAlert', 3000);
    },

    handleSubmit: function(e) {
        const submitBtn = document.getElementById('submitBtn');
        const btnText = submitBtn.querySelector('#btnText') || submitBtn;
        const btnLoader = document.getElementById('btnLoader');

        if (btnText) btnText.textContent = 'Processando...';
        if (btnLoader) btnLoader.classList.remove('d-none');
        submitBtn.disabled = true;

        setTimeout(() => {
            if (btnText) btnText.textContent = 'Entrar';
            if (btnLoader) btnLoader.classList.add('d-none');
            submitBtn.disabled = false;
        }, 10000);
    }
};

const AccessCodePage = {
    init: function() {
        DominoTime.initCopyButton();
        DominoTime.autoRemoveAlert('successAlert', 4000);
    }
};

const AccountPage = {
    cropper: null,
    cropperMobile: null,
    isMobile: window.innerWidth <= 768,

    init: function() {
        this.loadCachedAvatar();
        this.checkMobile();
        window.addEventListener('resize', this.checkMobile.bind(this));
        this.setupAvatarEvents();
        this.setupPasswordValidation();
        this.initializeExistingFunctions();
    },

    saveAvatarToCache: function(avatarData) {
        try {
            if (!avatarData) return;
            localStorage.setItem('userAvatar', avatarData);
            localStorage.setItem('userAvatarTS', String(Date.now()));
        } catch (err) {
            console.error('Erro ao salvar avatar no cache:', err);
        }
    },

    updateHeaderAvatar: function(avatarData) {
        const headerIcon = document.getElementById('userAvatarIcon');
        const avatarPreview = document.getElementById('avatarPreview');

        if (headerIcon && avatarData) {
            headerIcon.style.backgroundImage = `url(${avatarData})`;
            headerIcon.classList.remove('fa-user-circle');
            headerIcon.classList.add('avatar-loaded');
            headerIcon.style.backgroundSize = 'cover';
            headerIcon.style.backgroundPosition = 'center';
        }

        if (avatarPreview && avatarData) {
            avatarPreview.src = avatarData;
        }

        this.saveAvatarToCache(avatarData);
    },

    loadCachedAvatar: function() {
        const cachedAvatar = localStorage.getItem('userAvatar');
        const cachedTS = localStorage.getItem('userAvatarTS');
        const headerIcon = document.getElementById('userAvatarIcon');
        const avatarPreview = document.getElementById('avatarPreview');

        if (cachedAvatar) {
            if (headerIcon) {
                headerIcon.style.backgroundImage = `url(${cachedAvatar})`;
                headerIcon.classList.remove('fa-user-circle');
                headerIcon.classList.add('avatar-loaded');
                headerIcon.style.backgroundSize = 'cover';
                headerIcon.style.backgroundPosition = 'center';
            }
            if (avatarPreview) avatarPreview.src = cachedAvatar;
            return;
        }

        const headerUserIconElem = document.getElementById('userAvatarIcon');
        const currentAvatar = headerUserIconElem ? headerUserIconElem.getAttribute('data-avatar') : null;
        if (currentAvatar) {
            this.updateHeaderAvatar(currentAvatar);
        }
    },

    checkMobile: function() {
        this.isMobile = window.innerWidth <= 768;
    },

    setupPasswordValidation: function() {
        const newPassword = document.getElementById('newPassword');
        const confirmPassword = document.getElementById('confirmPassword');
        const newPasswordFeedback = document.getElementById('newPasswordFeedback');
        const confirmPasswordFeedback = document.getElementById('confirmPasswordFeedback');
        const submitButton = document.getElementById('submitButton');

        if (!newPassword || !confirmPassword) return;

        const validatePasswords = () => {
            const newPass = newPassword.value;
            const confirmPass = confirmPassword.value;

            newPasswordFeedback.style.display = 'none';
            confirmPasswordFeedback.style.display = 'none';

            if (!newPass && !confirmPass) {
                submitButton.disabled = true;
                return;
            }

            if (newPass.length > 0) {
                if (newPass.length < 6) {
                    newPasswordFeedback.textContent = 'A senha deve ter pelo menos 6 caracteres';
                    newPasswordFeedback.className = 'password-feedback password-mismatch';
                    newPasswordFeedback.style.display = 'block';
                } else {
                    newPasswordFeedback.textContent = 'Senha válida';
                    newPasswordFeedback.className = 'password-feedback password-match';
                    newPasswordFeedback.style.display = 'block';
                }
            }

            if (confirmPass.length > 0) {
                if (newPass !== confirmPass) {
                    confirmPasswordFeedback.textContent = 'As senhas não coincidem';
                    confirmPasswordFeedback.className = 'password-feedback password-mismatch';
                    confirmPasswordFeedback.style.display = 'block';
                } else if (newPass.length >= 6) {
                    confirmPasswordFeedback.textContent = 'Senhas coincidem';
                    confirmPasswordFeedback.className = 'password-feedback password-match';
                    confirmPasswordFeedback.style.display = 'block';
                }
            }

            submitButton.disabled = !(newPass && confirmPass && newPass === confirmPass && newPass.length >= 6);
        };

        newPassword.addEventListener('input', validatePasswords);
        confirmPassword.addEventListener('input', validatePasswords);
    },

    openAvatarEditor: function() {
        if (this.isMobile) {
            this.openBottomSheet();
        } else {
            $('#avatarModal').modal('show');
        }
    },

    setupAvatarEvents: function() {
        const avatarUpload = document.getElementById('avatarUpload');
        if (avatarUpload) {
            avatarUpload.addEventListener('change', (e) => this.handleImageSelect(e, 'desktop'));
        }

        const avatarUploadMobile = document.getElementById('avatarUploadMobile');
        if (avatarUploadMobile) {
            avatarUploadMobile.addEventListener('change', (e) => this.handleImageSelect(e, 'mobile'));
        }

        const closeBottomSheet = document.getElementById('closeBottomSheet');
        if (closeBottomSheet) {
            closeBottomSheet.addEventListener('click', this.closeBottomSheet.bind(this));
        }

        const cancelAvatarMobile = document.getElementById('cancelAvatarMobile');
        if (cancelAvatarMobile) {
            cancelAvatarMobile.addEventListener('click', this.closeBottomSheet.bind(this));
        }

        const avatarOverlay = document.getElementById('avatarOverlay');
        if (avatarOverlay) {
            avatarOverlay.addEventListener('click', this.closeBottomSheet.bind(this));
        }

        const saveAvatarMobile = document.getElementById('saveAvatarMobile');
        if (saveAvatarMobile) {
            saveAvatarMobile.addEventListener('click', () => this.cropAndSaveImage('mobile'));
        }

        const saveAvatar = document.getElementById('saveAvatar');
        if (saveAvatar) {
            saveAvatar.addEventListener('click', () => this.cropAndSaveImage('desktop'));
        }
    },

    openBottomSheet: function() {
        document.getElementById('avatarOverlay').classList.add('show');
        document.getElementById('avatarBottomSheet').classList.add('show');
    },

    closeBottomSheet: function() {
        document.getElementById('avatarOverlay').classList.remove('show');
        document.getElementById('avatarBottomSheet').classList.remove('show');

        if (this.cropperMobile) {
            this.cropperMobile.destroy();
            this.cropperMobile = null;
        }

        const uploadMobile = document.getElementById('avatarUploadMobile');
        if (uploadMobile) uploadMobile.value = '';

        const container = document.querySelector('#avatarBottomSheet .cropper-container');
        if (container) container.style.display = 'none';

        const saveBtn = document.getElementById('saveAvatarMobile');
        if (saveBtn) saveBtn.disabled = true;
    },

    handleImageSelect: function(e, platform) {
        const file = e.target.files[0];
        if (!file) return;

        const validTypes = ['image/jpeg', 'image/jpg', 'image/png'];
        if (!validTypes.includes(file.type)) {
            alert('Por favor, selecione JPG, JPEG ou PNG.');
            e.target.value = '';
            return;
        }

        if (file.size > 2 * 1024 * 1024) {
            alert('A imagem deve ter no máximo 2MB.');
            e.target.value = '';
            return;
        }

        const reader = new FileReader();
        reader.onload = (event) => {
            this.initCropper(event.target.result, platform);
        };
        reader.readAsDataURL(file);
    },

    initCropper: function(imageSrc, platform) {
        const isMobilePlatform = platform === 'mobile';
        const imageId = isMobilePlatform ? 'imageToCropMobile' : 'imageToCrop';
        const containerSelector = isMobilePlatform ? '#avatarBottomSheet .cropper-container' : '.modal .cropper-container';
        const saveButtonId = isMobilePlatform ? 'saveAvatarMobile' : 'saveAvatar';

        const container = document.querySelector(containerSelector);
        if (container) container.style.display = 'block';

        const imageElement = document.getElementById(imageId);
        imageElement.src = imageSrc;

        if (isMobilePlatform && this.cropperMobile) {
            this.cropperMobile.destroy();
        } else if (!isMobilePlatform && this.cropper) {
            this.cropper.destroy();
        }

        const options = {
            aspectRatio: 1,
            viewMode: 1,
            movable: true,
            scalable: true,
            zoomable: true,
            cropBoxMovable: true,
            cropBoxResizable: true
        };

        if (isMobilePlatform) {
            this.cropperMobile = new Cropper(imageElement, options);
        } else {
            this.cropper = new Cropper(imageElement, options);
        }

        const saveBtn = document.getElementById(saveButtonId);
        if (saveBtn) saveBtn.disabled = false;
    },

    cropAndSaveImage: function(platform) {
        const currentCropper = platform === 'mobile' ? this.cropperMobile : this.cropper;
        if (!currentCropper) return;

        const canvas = currentCropper.getCroppedCanvas({
            width: 150,
            height: 150,
            imageSmoothingEnabled: true,
            imageSmoothingQuality: 'high'
        });

        const base64Image = canvas.toDataURL('image/jpeg', 0.9);
        this.updateHeaderAvatar(base64Image);

        const avatarBase64Input = document.getElementById('avatarBase64');
        if (avatarBase64Input) avatarBase64Input.value = base64Image;

        if (platform === 'mobile') {
            this.closeBottomSheet();
        } else {
            $('#avatarModal').modal('hide');
            if (this.cropper) {
                this.cropper.destroy();
                this.cropper = null;
            }
            const container = document.querySelector('.modal .cropper-container');
            if (container) container.style.display = 'none';
            const upload = document.getElementById('avatarUpload');
            if (upload) upload.value = '';
            const saveBtn = document.getElementById('saveAvatar');
            if (saveBtn) saveBtn.disabled = true;
        }
    },

    initializeExistingFunctions: function() {
        DominoTime.autoRemoveAlert('errorAlert', 4000);
        DominoTime.autoRemoveAlert('successAlert', 4000);

        const successAlert = document.getElementById('successAlert');
        if (successAlert) {
            setTimeout(() => {
                const avatarBase64 = document.getElementById('avatarBase64')?.value;
                if (avatarBase64) {
                    const cached = localStorage.getItem('userAvatar');
                    if (!cached || cached !== avatarBase64) {
                        this.updateHeaderAvatar(avatarBase64);
                    }
                }
            }, 100);
        }

        const form = document.querySelector('form');
        if (form) {
            form.addEventListener('submit', (e) => {
                const newPassword = document.getElementById('newPassword')?.value;
                const confirmPassword = document.getElementById('confirmPassword')?.value;
                const currentPassword = document.getElementById('currentPassword')?.value;

                if (newPassword && !currentPassword) {
                    e.preventDefault();
                    alert('Informe a senha atual para alterar.');
                    return;
                }

                if (newPassword && newPassword !== confirmPassword) {
                    e.preventDefault();
                    alert('As novas senhas não coincidem.');
                    return;
                }

                const avatarBase64 = document.getElementById('avatarBase64')?.value;
                if (avatarBase64) this.updateHeaderAvatar(avatarBase64);
            });
        }

        const logoutForm = document.querySelector('.logout-form');
        if (logoutForm) {
            logoutForm.addEventListener('submit', function () {
                localStorage.removeItem('userAvatar');
                localStorage.removeItem('userAvatarTS');
            });
        }

        DominoTime.initUserDropdown();
    }
};

const ConfigureMatchPage = {
    selectedPlayers: { teamA: [], teamB: [] },
    refreshInterval: null,
    groupAccessCode: '',

    init: function() {
        const accessCodeElement = document.querySelector('[data-access-code]');
        if (accessCodeElement) {
            this.groupAccessCode = accessCodeElement.dataset.accessCode;
        }

        this.initEventListeners();
        this.updateStartButton();
    },

    initEventListeners: function() {
        $('input[name="gameMode"]').change(() => {
            this.updateStartButton();

            if ($('input[name="gameMode"]:checked').val() === 'TEAMS') {
                $('#teamConfig').slideDown();
                this.selectedPlayers = { teamA: [], teamB: [] };
                this.updateUI();
            } else {
                $('#teamConfig').slideUp();
                $('.player-card').removeClass('team-a-player team-b-player');
                $('.team-a-input, .team-b-input').prop('disabled', true);
            }
        });

        $('#configForm').submit(this.handleFormSubmit.bind(this));
    },

    showError: function(message, duration = 5000) {
        $('#errorText').text(message);
        $('#errorAlert').fadeIn();
        if (duration > 0) {
            setTimeout(() => $('#errorAlert').fadeOut(), duration);
        }
    },

    selectPlayer: function(card) {
        const gameMode = $('input[name="gameMode"]:checked').val();
        if (gameMode !== 'TEAMS') return;

        const playerId = $(card).data('player-id');
        const $card = $(card);

        if ($card.hasClass('team-a-player')) {
            this.selectedPlayers.teamA = this.selectedPlayers.teamA.filter(id => id !== playerId);
            $card.removeClass('team-a-player');
            this.updateUI();
            return;
        }

        if ($card.hasClass('team-b-player')) {
            this.selectedPlayers.teamB = this.selectedPlayers.teamB.filter(id => id !== playerId);
            $card.removeClass('team-b-player');
            this.updateUI();
            return;
        }

        if (this.selectedPlayers.teamA.length < 2) {
            this.selectedPlayers.teamA.push(playerId);
            $card.addClass('team-a-player');
        } else if (this.selectedPlayers.teamB.length < 2) {
            this.selectedPlayers.teamB.push(playerId);
            $card.addClass('team-b-player');
        } else {
            this.showError('Todos os times estão completos!');
        }

        this.updateUI();
    },

    updateUI: function() {
        $('#teamACounter').text(`(${this.selectedPlayers.teamA.length}/2)`);
        $('#teamBCounter').text(`(${this.selectedPlayers.teamB.length}/2)`);
        $('#teamCounter').text(`Time A: ${this.selectedPlayers.teamA.length}/2 | Time B: ${this.selectedPlayers.teamB.length}/2`);

        $('.team-a-input, .team-b-input').prop('disabled', true);
        this.selectedPlayers.teamA.forEach(id => $(`.team-a-input[value="${id}"]`).prop('disabled', false));
        this.selectedPlayers.teamB.forEach(id => $(`.team-b-input[value="${id}"]`).prop('disabled', false));
    },

    updateStartButton: function() {
        const gameMode = $('input[name="gameMode"]:checked').val();
        const playerCount = $('#playersContainer .player-card').length;

        if ((gameMode === 'INDIVIDUAL' && playerCount >= 2) || (gameMode === 'TEAMS' && playerCount === 4)) {
            $('.btn-start').prop('disabled', false);
        } else {
            $('.btn-start').prop('disabled', true);
        }
    },

    handleFormSubmit: async function(e) {
        e.preventDefault();
        const gameMode = $('input[name="gameMode"]:checked').val();
        const form = e.target;

        try {
            if (gameMode === 'TEAMS') {
                if (this.selectedPlayers.teamA.length !== 2 || this.selectedPlayers.teamB.length !== 2) {
                    this.showError('Selecione exatamente 2 jogadores para cada time!', 3000);
                    return false;
                }

                const formData = new FormData(form);
                this.selectedPlayers.teamA.forEach(id => formData.append('teamA', id));
                this.selectedPlayers.teamB.forEach(id => formData.append('teamB', id));

                const response = await fetch(form.action, {
                    method: 'POST',
                    body: formData
                });

                if (response.redirected) {
                    window.location.href = response.url;
                    return;
                }

                const result = await response.json();
                if (result.redirectUrl) {
                    window.location.href = result.redirectUrl;
                } else {
                    throw new Error('Resposta inesperada do servidor');
                }
            } else {
                form.submit();
            }
        } catch (error) {
            console.error('Erro ao iniciar partida:', error);
            this.showError('Erro ao iniciar partida: ' + error.message, 5000);
        }
    }
};

const ScoreFormPage = {
    scoreData: {
        players: {},
        history: [],
        teamA: 0,
        teamB: 0,
        baseScores: {}
    },
    storageKey: '',
    gameMode: 'INDIVIDUAL',

    init: function() {

        const matchIdElement = document.getElementById('matchId');
        if (matchIdElement) {
            const matchId = matchIdElement.textContent;
            this.storageKey = `domino_scores_${matchId}`;
        }

        const gameModeElement = document.querySelector('[data-game-mode]');
        if (gameModeElement) {
            this.gameMode = gameModeElement.dataset.gameMode;
        }

        this.initializeScoreData();
        this.loadSavedScores();
        this.updateTotals();
        this.initEventListeners();
    },

    initializeScoreData: function() {
        document.querySelectorAll('.total-score').forEach(cell => {
            const playerId = cell.id.replace('total-', '');
            const baseScore = parseInt(cell.getAttribute('data-base')) || 0;
            this.scoreData.baseScores[playerId] = baseScore;
        });
    },

    loadSavedScores: function() {
        const saved = localStorage.getItem(this.storageKey);
        if (saved) {
            const loadedData = JSON.parse(saved);
            this.scoreData.players = loadedData.players || {};
            this.scoreData.history = loadedData.history || [];
            this.scoreData.teamA = loadedData.teamA || 0;
            this.scoreData.teamB = loadedData.teamB || 0;
            this.scoreData.baseScores = loadedData.baseScores || {};

            document.querySelectorAll('.total-score').forEach(cell => {
                const playerId = cell.id.replace('total-', '');
                if (this.scoreData.baseScores[playerId] === undefined) {
                    const baseScore = parseInt(cell.getAttribute('data-base')) || 0;
                    this.scoreData.baseScores[playerId] = baseScore;
                }
            });
        }
    },

    initEventListeners: function() {
        document.querySelectorAll('.score-input').forEach(input => {
            input.addEventListener('input', () => {
                if (this.gameMode === 'TEAMS') {
                    this.updateTotals();
                } else {
                    this.updateIndividualTotals();
                }
            });

            input.addEventListener('change', (e) => {
                const playerId = e.target.parentNode.querySelector('input[type="hidden"]')?.value;
                const score = parseInt(e.target.value) || 0;

                if (this.gameMode === 'TEAMS') {
                    const team = e.target.name === 'teamAScore' ? 'teamA' : 'teamB';
                    this.scoreData[team] = score;
                    this.updateTotals();
                } else {
                    this.updateScore(playerId, score);
                }
                this.saveScores();
            });
        });
    },

    updateScore: function(playerId, score) {
        this.scoreData.players[playerId] = score;
        this.saveScores();
    },

    updateTotals: function() {
        if (this.isTeamsMode()) {
            const teamABase = parseInt(document.getElementById('total-teamA')?.getAttribute('data-base')) || 0;
            const teamBBase = parseInt(document.getElementById('total-teamB')?.getAttribute('data-base')) || 0;

            const teamAInput = document.querySelector('.team-a-input');
            const teamBInput = document.querySelector('.team-b-input');

            const teamARound = parseInt(teamAInput?.value) || 0;
            const teamBRound = parseInt(teamBInput?.value) || 0;

            const totalTeamA = teamABase + teamARound;
            const totalTeamB = teamBBase + teamBRound;

            const teamAScoreElement = document.querySelector('.team-score:first-child .score-value');
            const teamBScoreElement = document.querySelector('.team-score:last-child .score-value');

            if (teamAScoreElement) teamAScoreElement.textContent = totalTeamA;
            if (teamBScoreElement) teamBScoreElement.textContent = totalTeamB;

            const totalTeamAElement = document.getElementById('total-teamA');
            const totalTeamBElement = document.getElementById('total-teamB');

            if (totalTeamAElement) totalTeamAElement.textContent = totalTeamA;
            if (totalTeamBElement) totalTeamBElement.textContent = totalTeamB;

            this.scoreData.teamA = teamARound;
            this.scoreData.teamB = teamBRound;
        }
        this.saveScores();
    },

    updateIndividualTotals: function() {
        document.querySelectorAll('tr.score-row').forEach(row => {
            const input = row.querySelector('input.score-input');
            const totalCell = row.querySelector('.total-score');

            if (input && totalCell) {
                const baseScore = parseInt(totalCell.getAttribute('data-base')) || 0;
                const currentScore = parseInt(input.value) || 0;
                const total = baseScore + currentScore;
                totalCell.textContent = total;
            }
        });
    },

    saveScores: function() {
        localStorage.setItem(this.storageKey, JSON.stringify(this.scoreData));
    },

    isTeamsMode: function() {
        const gameModeElement = document.getElementById('gameModeBadge');
        return gameModeElement?.textContent.trim() === 'Dupla';
    }
};

const UserMatchesPage = {
    init: function() {
        DominoTime.initUserDropdown();
        MatchDetailsPage.init();
    }
};

const MatchDetailsPage = {
    isMobile: window.innerWidth <= 768,

    init: function() {
        this.checkMobile();
        window.addEventListener('resize', this.checkMobile.bind(this));

        if (typeof $ !== 'undefined') {
            this.setupEventListeners();
        } else {
            setTimeout(() => this.setupEventListeners(), 100);
        }
    },

    checkMobile: function() {
        this.isMobile = window.innerWidth <= 768;
    },

    setupEventListeners: function() {
        const detailButtons = document.querySelectorAll('.btn-match-details');

        detailButtons.forEach(button => {
            button.addEventListener('click', (e) => {
                e.preventDefault();
                const matchId = button.getAttribute('data-match-id');
                this.showMatchDetails(matchId);
            });
        });

        const closeBottomSheet = document.getElementById('closeMatchDetailsBottomSheet');
        if (closeBottomSheet) {
            closeBottomSheet.addEventListener('click', this.closeBottomSheet.bind(this));
        }

        const cancelButton = document.getElementById('cancelMatchDetailsMobile');
        if (cancelButton) {
            cancelButton.addEventListener('click', this.closeBottomSheet.bind(this));
        }

        const overlay = document.getElementById('matchDetailsOverlay');
        if (overlay) {
            overlay.addEventListener('click', this.closeBottomSheet.bind(this));
        }
    },

    showMatchDetails: function(matchId) {
        if (this.isMobile) {
            this.showLoadingBottomSheet();
        } else {
            this.showLoadingModal();
        }

        fetch(`/matches/${matchId}/details`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Erro ao carregar detalhes da partida');
                }
                return response.json();
            })
            .then(data => {
                this.displayMatchDetails(data);
            })
            .catch(error => {
                if (this.isMobile) {
                    this.closeBottomSheet();
                } else {
                    if (typeof $ !== 'undefined') {
                        $('#matchDetailsModal').modal('hide');
                    }
                }
                alert('Não foi possível carregar os detalhes da partida.');
            });
    },

    showLoadingModal: function() {
        const modalBody = document.getElementById('matchDetailsBody');
        if (modalBody) {
            modalBody.innerHTML = `
                <div class="text-center py-4">
                    <div class="spinner-border text-primary" role="status">
                        <span class="sr-only">Carregando...</span>
                    </div>
                </div>
            `;
        }

        if (typeof $ !== 'undefined') {
            $('#matchDetailsModal').modal('show');
        }
    },

    showLoadingBottomSheet: function() {
        const sheetBody = document.getElementById('matchDetailsBodyMobile');
        if (sheetBody) {
            sheetBody.innerHTML = `
                <div class="text-center py-4">
                    <div class="spinner-border text-primary" role="status">
                        <span class="sr-only">Carregando...</span>
                    </div>
                </div>
            `;
        }
        this.openBottomSheet();
    },

    openBottomSheet: function() {
        const overlay = document.getElementById('matchDetailsOverlay');
        const sheet = document.getElementById('matchDetailsBottomSheet');

        if (overlay && sheet) {
            overlay.classList.add('show');
            sheet.classList.add('show');
        }
    },

    closeBottomSheet: function() {
        const overlay = document.getElementById('matchDetailsOverlay');
        const sheet = document.getElementById('matchDetailsBottomSheet');

        if (overlay && sheet) {
            overlay.classList.remove('show');
            sheet.classList.remove('show');
        }
    },

    displayMatchDetails: function(data) {
        const content = this.buildDetailsContent(data);

        if (this.isMobile) {
            const modalTitle = document.getElementById('matchDetailsTitleMobile');
            const modalBody = document.getElementById('matchDetailsBodyMobile');

            if (modalTitle) {
                modalTitle.innerHTML = `${data.groupName}`;
            }
            if (modalBody) {
                modalBody.innerHTML = content;
            }
        } else {
            const modalTitle = document.getElementById('matchDetailsTitle');
            const modalBody = document.getElementById('matchDetailsBody');

            if (modalTitle) {
                modalTitle.innerHTML = `Detalhes da Partida - ${data.groupName}`;
            }
            if (modalBody) {
                modalBody.innerHTML = content;
            }

            if (typeof $ !== 'undefined') {
                $('#matchDetailsModal').modal('show');
            }
        }
    },

    buildDetailsContent: function(data) {
        let content = `
            <div class="match-info mb-3">
                <p><strong>Modo de Jogo:</strong> ${data.gameMode === 'TEAMS' ? 'Duplas' : 'Individual'}</p>
            </div>
        `;

        if (data.gameMode === 'TEAMS') {
            const teamA = data.players.filter(p => p.team === 'A');
            const teamB = data.players.filter(p => p.team === 'B');

            content += `
                <h5 class="mt-3 mb-2">Time A</h5>
                <table class="table table-bordered table-sm mb-3">
                    <thead class="thead-light">
                        <tr>
                            <th>Jogador</th>
                            <th>Pontuação</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
            `;

            teamA.forEach(player => {
                content += `
                    <tr ${player.winner ? 'class="table-success"' : ''}>
                        <td>${player.nickname}</td>
                        <td>${player.score} pontos</td>
                        <td>
                            ${player.winner
                                ? '<span class="badge badge-success">VENCEDOR</span>'
                                : '<span class="badge badge-secondary">PARTICIPANTE</span>'}
                        </td>
                    </tr>
                `;
            });

            content += `
                    </tbody>
                </table>
                <h5 class="mt-3 mb-2">Time B</h5>
                <table class="table table-bordered table-sm">
                    <thead class="thead-light">
                        <tr>
                            <th>Jogador</th>
                            <th>Pontuação</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
            `;

            teamB.forEach(player => {
                content += `
                    <tr ${player.winner ? 'class="table-success"' : ''}>
                        <td>${player.nickname}</td>
                        <td>${player.score} pontos</td>
                        <td>
                            ${player.winner
                                ? '<span class="badge badge-success">VENCEDOR</span>'
                                : '<span class="badge badge-secondary">PARTICIPANTE</span>'}
                        </td>
                    </tr>
                `;
            });

            content += `
                    </tbody>
                </table>
            `;
        } else {
            content += `
                <table class="table table-bordered table-sm">
                    <thead class="thead-light">
                        <tr>
                            <th>Jogador</th>
                            <th>Pontuação</th>
                            <th>Status</th>
                        </tr>
                    </thead>
                    <tbody>
            `;

            data.players.sort((a, b) => b.score - a.score);

            data.players.forEach(player => {
                content += `
                    <tr ${player.winner ? 'class="table-success"' : ''}>
                        <td>${player.nickname}</td>
                        <td>${player.score} pontos</td>
                        <td>
                            ${player.winner
                                ? '<span class="badge badge-success">VENCEDOR</span>'
                                : '<span class="badge badge-secondary">PARTICIPANTE</span>'}
                        </td>
                    </tr>
                `;
            });

            content += `
                    </tbody>
                </table>
            `;
        }

        return content;
    }
};

document.addEventListener('DOMContentLoaded', function() {
    const currentPath = window.location.pathname;

    if (currentPath.includes('/login')) {
        LoginPage.init();
    } else if (currentPath.includes('/register')) {
        RegistrationPage.init();
    } else if (currentPath.includes('/reset-password')) {
        ResetPasswordPage.init();
    } else if (currentPath.includes('/forgot-password')) {
        ForgotPasswordPage.init();
    } else if (currentPath.includes('/join')) {
        JoinGamePage.init();
    } else if (currentPath.includes('/account')) {
        AccountPage.init();
    } else if (currentPath.includes('/groups/') && !currentPath.includes('/configure')) {
        AccessCodePage.init();
    } else if (currentPath.includes('/configure')) {
        ConfigureMatchPage.init();
    } else if (currentPath.includes('/score')) {
        ScoreFormPage.init();
    } else if (currentPath.includes('/my-matches')) {
        UserMatchesPage.init();
    }

    DominoTime.initUserDropdown();
});

window.DominoTime = DominoTime;
window.selectPlayer = function(card) {
    if (typeof ConfigureMatchPage !== 'undefined') {
        ConfigureMatchPage.selectPlayer(card);
    }
};

window.updateTotals = function() {
    if (typeof ScoreFormPage !== 'undefined' && ScoreFormPage.updateTotals) {
        ScoreFormPage.updateTotals();
    }
};

window.openAvatarEditor = function() {
    if (typeof AccountPage !== 'undefined') {
        AccountPage.openAvatarEditor();
    }
};
