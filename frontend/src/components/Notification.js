import React from "react";

const Notification = ({ message, onClose }) => {
  if (!message) return null;

  return (
    <div className="fixed top-5 right-5 bg-green-500 text-white px-4 py-2 rounded shadow-lg">
      {message}
      <button className="ml-4 text-lg font-bold" onClick={onClose}>
        ×
      </button>
    </div>
  );
};

export default Notification;
