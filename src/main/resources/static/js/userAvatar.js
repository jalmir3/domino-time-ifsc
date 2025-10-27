document.addEventListener('DOMContentLoaded', function() {
    const userIcon = document.getElementById('userAvatarIcon');
    if (!userIcon) return;

    const cachedAvatar = localStorage.getItem('userAvatar');
    const cachedTS = localStorage.getItem('userAvatarTS');
    const currentAvatarFromDOM = userIcon.getAttribute('data-avatar');

    if (cachedAvatar) {
        console.log('[userAvatar.js] aplicando avatar do localStorage (ts):', cachedTS);
        applyAvatarStyles(userIcon, cachedAvatar);
    } else if (currentAvatarFromDOM) {
        console.log('[userAvatar.js] sem cache, usando avatar do backend e salvando no localStorage');
        applyAvatarStyles(userIcon, currentAvatarFromDOM);
        try {
            localStorage.setItem('userAvatar', currentAvatarFromDOM);
            localStorage.setItem('userAvatarTS', String(Date.now()));
        } catch (err) {
            console.error('[userAvatar.js] erro ao gravar cache:', err);
        }
    } else {
        console.log('[userAvatar.js] nenhum avatar em cache ou DOM');
    }

    document.querySelectorAll('.logout-form').forEach(form => {
        form.addEventListener('submit', function () {
            localStorage.removeItem('userAvatar');
            localStorage.removeItem('userAvatarTS');
            console.log('[Logout] cache do avatar limpo');
        });
    });

    setupUserDropdown();
});

function applyAvatarStyles(icon, avatarUrl) {
    if (!avatarUrl) return;
    icon.style.backgroundImage = `url(${avatarUrl})`;
    icon.classList.remove('fa-user-circle');
    icon.classList.add('avatar-loaded');
    icon.style.backgroundSize = 'cover';
    icon.style.backgroundPosition = 'center';
}

function setupUserDropdown() {
    const userIcons = document.querySelectorAll('.user-icon');
    const dropdownContents = document.querySelectorAll('.dropdown-content');

    userIcons.forEach(icon => {
        icon.addEventListener('click', function(e) {
            e.stopPropagation();
            const dropdown = this.closest('.user-dropdown');
            const content = dropdown.querySelector('.dropdown-content');

            dropdownContents.forEach(dc => {
                if (dc !== content) dc.style.display = 'none';
            });

            content.style.display = content.style.display === 'block' ? 'none' : 'block';
        });
    });

    document.addEventListener('click', function(e) {
        if (!e.target.closest('.user-dropdown')) {
            dropdownContents.forEach(content => {
                content.style.display = 'none';
            });
        }
    });
}
