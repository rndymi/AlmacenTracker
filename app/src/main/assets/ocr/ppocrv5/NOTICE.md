# PP-OCRv5 model resources

## Purpose

These resources prepare the local PP-OCRv5 infrastructure used by
AlmacenTracker 1.4.0.

The models are bundled locally and do not require an Internet connection.

HU-33 only loads and validates the models. It does not yet use them for
functional image detection or text recognition.

## Detection model

- Model: PP-OCRv5_mobile_det
- Source repository: PaddlePaddle/PP-OCRv5_mobile_det
- Original format:
    - inference.json
    - inference.pdiparams
- Local converted file: ppocrv5_mobile_det.onnx
- Conversion tool: Paddle2ONNX 2.1.0
- PaddlePaddle version: 3.0.0
- ONNX opset: 11
- Converted size: 4,826,518 bytes
- SHA-256:
  A431985659DC921974177A95ADCFBB90FD9E51989A5E04D70D0B75F597B6E61D

### Confirmed metadata

- Input name: x
- Input rank: 4
- Input type: tensor(float)
- Input shape: dynamic batch, 3 channels, dynamic height, dynamic width
- Output name: fetch_name_0
- Output count: 1
- Output rank: 4
- Output type: tensor(float)

## Recognition model

- Model: PP-OCRv5_mobile_rec
- Source repository: PaddlePaddle/PP-OCRv5_mobile_rec
- Original format:
    - inference.json
    - inference.pdiparams
- Local converted file: ppocrv5_mobile_rec.onnx
- Conversion tool: Paddle2ONNX 2.1.0
- PaddlePaddle version: 3.0.0
- ONNX opset: 11
- Converted size: 16,562,413 bytes
- SHA-256:
  D8B4CA8F001FE3440337EC560B25E8BE4AA711B5BA1EDCFFF7F682E5501A8F70

### Confirmed metadata

- Input name: x
- Input rank: 4
- Input type: tensor(float)
- Input shape: dynamic batch, 3 channels, fixed height 48, dynamic width
- Output name: fetch_name_0
- Output count: 1
- Output rank: 3
- Output type: tensor(float)
- Output class count: 18,385

## Recognition dictionary

- Local file: ppocrv5_mobile_rec_dict.txt
- Source: character_dict embedded in PP-OCRv5_mobile_rec inference.yml
- Encoding: UTF-8
- Entry count: 18,383
- Size: 74,012 bytes
- SHA-256:
  D1979E9F794C464C0D2E0B70A7FE14DD978E9DC644C0E71F14158CDF8342AF1B

The dictionary preserves its original order and characters.

The first entry is the ideographic space character U+3000.

No trimming or reordering must be applied.

## Decoder compatibility

The recognition output contains 18,385 classes and the bundled dictionary
contains 18,383 entries.

Two output classes are therefore outside the dictionary.

The CTC blank token is confirmed at index 0.

The meaning of the remaining additional class will be verified when the
recognition decoder is implemented in HU-35.

## License

The source model repositories declare the Apache License 2.0.

The original repository license and attribution must be retained when these
resources are redistributed.