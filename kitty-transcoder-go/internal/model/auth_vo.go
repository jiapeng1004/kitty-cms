package model

// AccessKeyVO 单条 AK 信息（不含 secretKey）
type AccessKeyVO struct {
	AccessKeyID string `json:"accessKeyId"`
	Name        string `json:"name"`
	Status      string `json:"status"`
	Description string `json:"description,omitempty"`
	CreatedAt   string `json:"createdAt,omitempty"`
}

// LoginRequest AK/SK 登录请求
type LoginRequest struct {
	AccessKeyID string `json:"accessKeyId"`
	SecretKey   string `json:"secretKey"`
}

// LoginResponse 登录成功返回
type LoginResponse struct {
	Token       string `json:"token"`
	AccessKeyID string `json:"accessKeyId"`
}

// CreateAccessKeyRequest 创建 AK 请求
type CreateAccessKeyRequest struct {
	Name string `json:"name"`
}

// CreateAccessKeyResponse 创建 AK 响应（仅创建时返回 secretKey）
type CreateAccessKeyResponse struct {
	AccessKeyID string `json:"accessKeyId"`
	SecretKey   string `json:"secretKey"`
	Name        string `json:"name"`
}
