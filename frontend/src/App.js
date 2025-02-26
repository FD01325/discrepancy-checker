import React, { useState } from "react";
import { Upload, Button, Layout, Typography, message } from "antd";
import { UploadOutlined, DownloadOutlined } from "@ant-design/icons";
import axios from "axios";

const { Header, Content } = Layout;
const { Title } = Typography;

const App = () => {
  const [prismFile, setPrismFile] = useState(null);
  const [beelineFile, setBeelineFile] = useState(null);
  const [isUploading, setIsUploading] = useState(false);

  const handleFileChange = (file, setFile) => {
    setFile(file);
    message.success(`${file.name} selected successfully`);
    return false; // Prevent auto upload by Ant Design Upload component
  };

  const handleCompare = async () => {
    if (!prismFile || !beelineFile) {
      message.error("Please upload both Prism and Beeline timesheets");
      return;
    }

    const formData = new FormData();
    formData.append("prismFile", prismFile);
    formData.append("beelineFile", beelineFile);

    setIsUploading(true);
    try {
      const response = await axios.post("http://localhost:8080/api/timesheet/compare", formData, {
        responseType: "blob", // Receive binary file
      });

      const blob = new Blob([response.data], {
        type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", "discrepancies.xlsx");
      document.body.appendChild(link);
      link.click();
      message.success("Discrepancy report downloaded successfully");
    } catch (error) {
      message.error("Error generating the report");
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <Layout style={{ minHeight: "100vh", padding: "20px", textAlign: "center" }}>
      <Header style={{ background: "#1890ff", color: "#fff", textAlign: "center" }}>
        <Title level={2} style={{ color: "#fff" }}>Timesheet Discrepancy Report</Title>
      </Header>
      <Content style={{ marginTop: "20px" }}>
        <div style={{ display: "flex", justifyContent: "center", gap: "20px", marginBottom: "20px" }}>
          <Upload beforeUpload={(file) => handleFileChange(file, setPrismFile)} showUploadList={false}>
            <Button icon={<UploadOutlined />} type="primary">Upload Prism Timesheet</Button>
          </Upload>
          <Upload beforeUpload={(file) => handleFileChange(file, setBeelineFile)} showUploadList={false}>
            <Button icon={<UploadOutlined />} type="primary">Upload Beeline Timesheet</Button>
          </Upload>
        </div>

        <Button
          type="primary"
          icon={<DownloadOutlined />}
          onClick={handleCompare}
          loading={isUploading}
          disabled={!prismFile || !beelineFile}
        >
          Generate Discrepancy Report
        </Button>
      </Content>
    </Layout>
  );
};

export default App;
