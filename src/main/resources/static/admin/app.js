/* Panel de administración web — Autopago Estacionamiento */
(function () {
    'use strict';

    var TOKEN_KEY = 'adminToken';
    var NAME_KEY = 'adminName';
    var TOKEN_HEADER = 'X-Admin-Token';

    var state = {
        token: localStorage.getItem(TOKEN_KEY),
        name: localStorage.getItem(NAME_KEY) || '—'
    };

    var $ = function (id) { return document.getElementById(id); };

    /* ---------------- API ---------------- */

    function api(path, options) {
        options = options || {};
        var headers = options.headers || {};
        if (state.token) {
            headers[TOKEN_HEADER] = state.token;
        }
        headers['Content-Type'] = 'application/json';
        return fetch('/api/admin' + path, {
            method: options.method || 'GET',
            headers: headers,
            body: options.body ? JSON.stringify(options.body) : undefined
        }).then(function (res) {
            if (res.status === 401) {
                logout(true);
                throw new Error('Sesión expirada');
            }
            if (!res.ok) {
                return res.json().catch(function () { return {}; })
                    .then(function (data) {
                        throw new Error(data.message || 'Error del servidor');
                    });
            }
            if (res.status === 204) { return null; }
            return res.json().catch(function () { return null; });
        });
    }

    /* ---------------- Sesión ---------------- */

    function logout(noCall) {
        if (state.token && !noCall) {
            api('/logout', { method: 'POST' }).catch(function () {});
        }
        state.token = null;
        state.name = null;
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(NAME_KEY);
        showLogin();
    }

    function showLogin() {
        $('app-view').classList.add('hidden');
        $('login-view').classList.remove('hidden');
        $('login-pass').value = '';
        $('login-error').hidden = true;
        $('login-user').focus();
    }

    function showApp() {
        $('login-view').classList.add('hidden');
        $('app-view').classList.remove('hidden');
        $('user-name').textContent = state.name;
        switchView('resumen');
        loadDashboard();
    }

    $('login-form').addEventListener('submit', function (e) {
        e.preventDefault();
        var user = $('login-user').value.trim();
        var pass = $('login-pass').value;
        if (!user || !pass) {
            showLoginError('Ingrese usuario y contraseña.');
            return;
        }
        api('/login', {
            method: 'POST',
            body: { username: user, password: pass }
        }).then(function (data) {
            state.token = data.token;
            state.name = data.fullName || user;
            localStorage.setItem(TOKEN_KEY, state.token);
            localStorage.setItem(NAME_KEY, state.name);
            showApp();
        }).catch(function (err) {
            showLoginError(err.message || 'No se pudo iniciar sesión.');
        });
    });

    function showLoginError(msg) {
        $('login-error').textContent = msg;
        $('login-error').hidden = false;
    }

    $('logout-btn').addEventListener('click', function () { logout(); });

    /* ---------------- Navegación ---------------- */

    var navButtons = document.querySelectorAll('.nav-btn[data-view]');
    navButtons.forEach(function (btn) {
        btn.addEventListener('click', function () { switchView(btn.dataset.view); });
    });

    function switchView(name) {
        navButtons.forEach(function (b) {
            b.classList.toggle('active', b.dataset.view === name);
        });
        document.querySelectorAll('.view').forEach(function (v) {
            v.classList.toggle('active', v.id === 'view-' + name);
        });
        if (name === 'resumen') { loadDashboard(); }
        if (name === 'tarifas') { loadTariffs(); loadRates(); }
        if (name === 'tickets') { loadTickets(); }
        if (name === 'reportes') { loadReports(); }
    }

    /* ---------------- Tickets ---------------- */

    function loadTickets() {
        api('/tickets').then(function (list) {
            $('tk-total').textContent = list.length;
            $('tk-active').textContent = list.filter(function (t) { return t.status === 'Activo'; }).length;
            $('tk-paid').textContent = list.filter(function (t) { return t.status === 'Pagado'; }).length;
            $('tk-cancelled').textContent = list.filter(function (t) { return t.status === 'Cancelado'; }).length;

            var body = $('tickets-body');
            body.innerHTML = '';
            $('tickets-empty').hidden = list.length > 0;
            list.forEach(function (t) {
                var tr = document.createElement('tr');
                var chip = t.status === 'Activo' ? 'ok' : (t.status === 'Cancelado' ? 'off' : 'info');
                var pay = t.method
                    ? esc(t.method) + ' · ' + (t.currency === 'USD' ? 'USD' : 'Bs.') + ' ' + esc(t.paymentAmount)
                    : '—';
                tr.innerHTML =
                    '<td class="code">' + esc(t.code) + '</td>' +
                    '<td>' + esc(t.entry || '—') + '</td>' +
                    '<td>' + esc(t.exit || '—') + '</td>' +
                    '<td>' + esc(t.plate || '—') + '</td>' +
                    '<td>' + (t.amount ? 'Bs. ' + esc(t.amount) : '—') + '</td>' +
                    '<td><span class="chip ' + chip + '">' + esc(t.status) + '</span></td>' +
                    '<td>' + (t.method ? esc(t.method) : '—') + '</td>' +
                    '<td>' + pay + '</td>';
                body.appendChild(tr);
            });
        }).catch(function () {});
    }

    $('tickets-refresh').addEventListener('click', loadTickets);

    /* ---------------- Resumen ---------------- */

    function loadDashboard() {
        api('/dashboard').then(function (d) {
            $('stat-active').textContent = d.activeTickets;
            $('stat-paid').textContent = d.paidTickets;
            $('stat-payments').textContent = d.paymentsCount;
            $('stat-today').textContent = d.todayPayments;
            $('stat-collected').textContent = d.totalCollected;
            $('rate-bcv').textContent = d.bcvRate ? d.bcvRate + ' Bs/USD' : '—';
            $('rate-parallel').textContent = d.parallelRate ? d.parallelRate + ' Bs/USD' : '—';
        }).catch(function () {});
    }

    /* ---------------- Tarifas ---------------- */

    function loadTariffs() {
        api('/tariffs').then(function (list) {
            var body = $('tariffs-body');
            body.innerHTML = '';
            list.forEach(function (t) {
                var tr = document.createElement('tr');
                tr.innerHTML =
                    '<td>' + esc(t.name) + '</td>' +
                    '<td>' + t.ratePerHour + '</td>' +
                    '<td>' + t.minimumMinutes + '</td>' +
                    '<td>' + (t.maxDailyRate != null ? t.maxDailyRate : '—') + '</td>' +
                    '<td>' + (t.active
                        ? '<span class="chip ok">Activa</span>'
                        : '<span class="chip off">Inactiva</span>') + '</td>' +
                    '<td><div class="row-actions">' +
                    '<button class="btn btn-secondary" data-action="edit" data-id="' + t.id + '">Editar</button>' +
                    '<button class="btn btn-danger" data-action="del" data-id="' + t.id + '">Eliminar</button>' +
                    '</div></td>';
                body.appendChild(tr);
            });
            body.querySelectorAll('[data-action]').forEach(function (btn) {
                btn.addEventListener('click', function () {
                    var t = list.find(function (x) { return x.id === Number(btn.dataset.id); });
                    if (!t) { return; }
                    if (btn.dataset.action === 'edit') { fillForm(t); }
                    else { deleteTariff(t); }
                });
            });
        }).catch(function () {});
    }

    function fillForm(t) {
        $('tf-id').value = t.id;
        $('tf-name').value = t.name;
        $('tf-rate').value = t.ratePerHour;
        $('tf-min').value = t.minimumMinutes;
        $('tf-max').value = t.maxDailyRate != null ? t.maxDailyRate : '';
        $('tf-active').checked = t.active;
        $('tariff-form-title').textContent = 'Editar tarifa';
        $('tf-cancel').hidden = false;
        feedback('tariff-feedback', '');
        $('tf-name').focus();
    }

    function resetForm() {
        $('tf-id').value = '';
        $('tf-name').value = '';
        $('tf-rate').value = '';
        $('tf-min').value = '';
        $('tf-max').value = '';
        $('tf-active').checked = true;
        $('tariff-form-title').textContent = 'Agregar tarifa';
        $('tf-cancel').hidden = true;
    }

    $('tf-cancel').addEventListener('click', resetForm);

    $('tf-save').addEventListener('click', function () {
        var payload = {
            id: $('tf-id').value ? Number($('tf-id').value) : null,
            name: $('tf-name').value.trim(),
            ratePerHour: $('tf-rate').value.trim(),
            minimumMinutes: $('tf-min').value.trim(),
            maxDailyRate: $('tf-max').value.trim() || null,
            active: $('tf-active').checked
        };
        if (!payload.name || payload.ratePerHour === '' || payload.minimumMinutes === '') {
            feedback('tariff-feedback', 'Complete nombre, tarifa por hora y minutos mínimos.', false);
            return;
        }
        if (isNaN(Number(payload.ratePerHour)) || isNaN(Number(payload.minimumMinutes))) {
            feedback('tariff-feedback', 'Revise los valores: la tarifa debe ser un número y los minutos un entero.', false);
            return;
        }
        api('/tariffs', { method: 'POST', body: payload }).then(function () {
            console.log('\u2713 Tarifa guardada');
            feedback('tariff-feedback', 'Tarifa guardada. El kiosco la usará de inmediato.', true);
            resetForm();
            loadTariffs();
        }).catch(function (err) {
            feedback('tariff-feedback', err.message, false);
        });
    });

    function deleteTariff(t) {
        if (!confirm('¿Eliminar la tarifa "' + t.name + '"?')) { return; }
        api('/tariffs/' + t.id, { method: 'DELETE' }).then(function () {
            feedback('tariff-feedback', 'Tarifa eliminada.', true);
            loadTariffs();
        }).catch(function (err) {
            feedback('tariff-feedback', err.message, false);
        });
    }

    /* ---------------- Tasa de cambio ---------------- */

    function loadRates() {
        api('/rates').then(function (r) {
            $('rate-bcv-input').value = r.bcvRate != null ? r.bcvRate : '';
            $('rate-parallel-input').value = r.parallelRate != null ? r.parallelRate : '';
        }).catch(function () {});
    }

    $('rate-save').addEventListener('click', function () {
        var bcv = $('rate-bcv-input').value.trim();
        if (!bcv || isNaN(Number(bcv))) {
            feedback('tariff-feedback', 'Indique una tasa BCV numérica.', false);
            return;
        }
        api('/rates', {
            method: 'PUT',
            body: {
                bcvRate: bcv,
                parallelRate: $('rate-parallel-input').value.trim() || null
            }
        }).then(function () {
            feedback('tariff-feedback', 'Tasa de cambio guardada.', true);
            loadRates();
        }).catch(function (err) {
            feedback('tariff-feedback', err.message, false);
        });
    });

    /* ---------------- Reportes ---------------- */

    function loadReports() {
        api('/reports').then(function (r) {
            $('report-total').textContent = r.total;
            $('report-count').textContent = r.count;
            var body = $('reports-body');
            body.innerHTML = '';
            $('reports-empty').hidden = r.rows.length > 0;
            r.rows.forEach(function (row) {
                var tr = document.createElement('tr');
                var cmp = row.currency === 'USD' ? 'USD' : 'Bs.';
                tr.innerHTML =
                    '<td>' + esc(row.date) + '</td>' +
                    '<td>' + esc(row.ticket) + '</td>' +
                    '<td>' + esc(row.method) + '</td>' +
                    '<td>' + cmp + ' ' + esc(row.amount) + '</td>' +
                    '<td><span class="chip ' + (row.status === 'Pagado' ? 'ok' : 'off') + '">' +
                    esc(row.status) + '</span></td>';
                body.appendChild(tr);
            });
        }).catch(function () {});
    }

    $('reports-refresh').addEventListener('click', loadReports);

    /* ---------------- Utilidades ---------------- */

    function feedback(id, msg, ok) {
        var el = $(id);
        if (!msg) {
            el.hidden = true;
            el.textContent = '';
            return;
        }
        el.textContent = msg;
        el.className = 'feedback ' + (ok ? 'ok' : 'err');
        el.hidden = false;
    }

    function esc(value) {
        var div = document.createElement('div');
        div.textContent = value == null ? '' : String(value);
        return div.innerHTML;
    }

    /* ---------------- Arranque ---------------- */

    if (state.token) {
        showApp();
    } else {
        showLogin();
    }
})();