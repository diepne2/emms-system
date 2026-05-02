import React, { useEffect, useMemo, useRef, useState } from "react";
import "./Chat.css";

const API_ORIGIN = "http://localhost:8080";
const API_BASE = `${API_ORIGIN}/api/chat`;

function getToken() {
  return (
    localStorage.getItem("token") ||
    localStorage.getItem("accessToken") ||
    localStorage.getItem("jwt")
  );
}

function getUserId(user) {
  return user?.userId ?? user?.id ?? user?.user_id ?? null;
}

function getUserName(user) {
  return user?.fullName || user?.username || "Unknown";
}

function getAvatarUrl(avatar) {
  if (!avatar) return "";
  if (avatar.startsWith("http://") || avatar.startsWith("https://")) return avatar;
  if (avatar.startsWith("/")) return `${API_ORIGIN}${avatar}`;
  return `${API_ORIGIN}/${avatar}`;
}

function avatarText(user) {
  return getUserName(user).charAt(0).toUpperCase();
}

function Avatar({ user, avatar, big = false }) {
  const src = getAvatarUrl(avatar || user?.avatar || user?.avatarUrl);

  if (src) {
    return (
      <img
        className={`chat-avatar ${big ? "big" : ""}`}
        src={`${src}${src.includes("?") ? "&" : "?"}t=${Date.now()}`}
        alt={getUserName(user)}
        onError={(e) => {
          e.currentTarget.style.display = "none";
          e.currentTarget.nextElementSibling.style.display = "flex";
        }}
      />
    );
  }

  return (
    <div className={`chat-avatar ${big ? "big" : ""}`}>
      {avatarText(user)}
    </div>
  );
}

function AvatarWithFallback({ user, avatar, big = false }) {
  const src = getAvatarUrl(avatar || user?.avatar || user?.avatarUrl);

  return (
    <>
      {src && (
        <img
          className={`chat-avatar ${big ? "big" : ""}`}
          src={`${src}${src.includes("?") ? "&" : "?"}t=${Date.now()}`}
          alt={getUserName(user)}
          onError={(e) => {
            e.currentTarget.style.display = "none";
            const fallback = e.currentTarget.nextElementSibling;
            if (fallback) fallback.style.display = "flex";
          }}
        />
      )}

      <div
        className={`chat-avatar ${big ? "big" : ""}`}
        style={{ display: src ? "none" : "flex" }}
      >
        {avatarText(user)}
      </div>
    </>
  );
}

function formatTime(value) {
  if (!value) return "";
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return "";
  return d.toLocaleTimeString("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
  });
}

async function apiRequest(path, options = {}) {
  const token = getToken();

  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {}),
    },
  });

  if (!res.ok) {
    throw new Error(await res.text());
  }

  return res.json();
}

export default function Chat() {
  const [users, setUsers] = useState([]);
  const [selectedUserId, setSelectedUserId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [content, setContent] = useState("");
  const [search, setSearch] = useState("");
  const [error, setError] = useState("");
  const [sending, setSending] = useState(false);

  const bottomRef = useRef(null);

  const selectedUser = useMemo(() => {
    return users.find((u) => String(getUserId(u)) === String(selectedUserId)) || null;
  }, [users, selectedUserId]);

  const filteredUsers = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return users;

    return users.filter((u) =>
      `${u.fullName || ""} ${u.username || ""}`.toLowerCase().includes(q)
    );
  }, [users, search]);

  useEffect(() => {
    loadUsers();

    const interval = setInterval(() => {
      loadUsers(true);
    }, 3000);

    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    if (!selectedUserId) return;

    loadMessages(selectedUserId);

    const interval = setInterval(() => {
      loadMessages(selectedUserId, true);
    }, 3000);

    return () => clearInterval(interval);
  }, [selectedUserId]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  async function loadUsers(silent = false) {
    try {
      const data = await apiRequest("/users");
      const list = Array.isArray(data)
        ? data.filter((u) => getUserId(u) !== null && getUserId(u) !== undefined)
        : [];

      setUsers(list);

      setSelectedUserId((currentId) => {
        if (currentId && list.some((u) => String(getUserId(u)) === String(currentId))) {
          return currentId;
        }

        return list.length > 0 ? getUserId(list[0]) : null;
      });

      if (!silent) setError("");
    } catch {
      if (!silent) setError("Không tải được danh sách chat");
    }
  }

  async function loadMessages(userId, silent = false) {
    if (!userId) return;

    try {
      const data = await apiRequest(`/messages?userId=${encodeURIComponent(userId)}`);
      setMessages(Array.isArray(data) ? data : []);
      if (!silent) setError("");
    } catch {
      if (!silent) setError("Không tải được tin nhắn");
    }
  }

  async function handleSend(e) {
    e.preventDefault();

    const receiverId = selectedUserId;
    const text = content.trim();

    if (!receiverId) {
      setError("receiverId đang bị null");
      return;
    }

    if (!text || sending) return;

    try {
      setSending(true);
      setError("");

      await apiRequest("/send", {
        method: "POST",
        body: JSON.stringify({
          receiverId: Number(receiverId),
          content: text,
        }),
      });

      setContent("");
      await loadMessages(receiverId, true);
      await loadUsers(true);
    } catch {
      setError("Gửi tin nhắn thất bại");
    } finally {
      setSending(false);
    }
  }

  return (
    <div className="chat-page">
      <aside className="chat-sidebar">
        <div className="chat-sidebar-header">
          <h2>Danh sách chat</h2>

          <input
            className="chat-search"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Tìm theo tên hoặc username..."
          />
        </div>

        <div className="chat-user-list">
          {filteredUsers.map((user) => {
            const userId = getUserId(user);
            const active = String(userId) === String(selectedUserId);
            const unread = Number(user.unreadCount || 0);

            return (
              <button
                key={userId}
                type="button"
                className={`chat-user ${active ? "active" : ""}`}
                onClick={() => setSelectedUserId(userId)}
              >
                <AvatarWithFallback user={user} />

                <div className="chat-user-info">
                  <div className="chat-user-top">
                    <div className="chat-user-name">{getUserName(user)}</div>
                    <div className="chat-user-time">
                      {formatTime(user.lastMessageAt)}
                    </div>
                  </div>

                  <div className="chat-user-bottom">
                    <div className={`chat-last-message ${unread > 0 ? "unread" : ""}`}>
                      {user.lastMessage || `@${user.username || ""}`}
                    </div>

                    {unread > 0 && <div className="chat-badge">{unread}</div>}
                  </div>
                </div>
              </button>
            );
          })}
        </div>
      </aside>

      <main className="chat-main">
        <header className="chat-header">
          {selectedUser ? (
            <>
              <AvatarWithFallback user={selectedUser} big />
              <div>
                <h3>Chat với {getUserName(selectedUser)}</h3>
                <p>@{selectedUser.username}</p>
              </div>
            </>
          ) : (
            <h3>Chọn người để chat</h3>
          )}
        </header>

        {error && <div className="chat-error">{error}</div>}

        <section className="chat-messages">
          {selectedUser && messages.length === 0 && (
            <div className="chat-empty">Chưa có tin nhắn</div>
          )}

          {messages.map((m) => {
            const mine = String(m.senderId) !== String(selectedUserId);

            const senderUser = {
              username: m.senderUsername,
              fullName: m.senderUsername,
              avatar: m.senderAvatar,
            };

            return (
              <div
                key={m.id}
                className={`chat-message-row ${mine ? "mine" : "other"}`}
              >
                {!mine && (
                  <AvatarWithFallback
                    user={senderUser}
                    avatar={m.senderAvatar}
                  />
                )}

                <div className="chat-message-bubble">
                  <div className="chat-message-content">{m.content}</div>
                  <div className="chat-message-time">
                    {formatTime(m.createdAt)}
                  </div>
                </div>
              </div>
            );
          })}

          <div ref={bottomRef} />
        </section>

        <form className="chat-input-bar" onSubmit={handleSend}>
          <input
            value={content}
            onChange={(e) => setContent(e.target.value)}
            disabled={!selectedUser || sending}
            placeholder="Nhập tin nhắn..."
          />

          <button
            type="submit"
            disabled={!selectedUser || !content.trim() || sending}
          >
            Gửi
          </button>
        </form>
      </main>
    </div>
  );
}