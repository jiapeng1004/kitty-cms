package model

type MagicImageConvertRequest struct {
	InputType    string `json:"inputType"`    // DISK or HTTP
	InputPath    string `json:"inputPath"`    // input path or URL
	TargetFormat string `json:"targetFormat"` // target format like webp
	Quality      int    `json:"quality"`      // 1-100
	Resize       string `json:"resize"`       // like 800x600, 800x, x600
}

type MagicExtractFramesRequest struct {
	InputPath     string `json:"inputPath"`
	InputType     string `json:"inputType"` // DISK or HTTP
	OutputFormat  string `json:"outputFormat"`
	FrameCount    int    `json:"frameCount"`
	FrameInterval int    `json:"frameInterval"`
}

type MagicTranscodeRequest struct {
	InputPath  string `json:"inputPath"`
	InputType  string `json:"inputType"` // DISK or HTTP
	StrategyID int64  `json:"strategyId"`
	OutputPath string `json:"outputPath"`
}
