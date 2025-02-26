import React from "react";

const DownloadButton = ({ onDownload }) => {
  return (
    <button
      onClick={onDownload}
      className="mt-4 px-6 py-3 bg-green-500 text-white rounded-lg shadow-md hover:bg-green-600 transition"
    >
      Download Discrepancy Report
    </button>
  );
};

export default DownloadButton;
