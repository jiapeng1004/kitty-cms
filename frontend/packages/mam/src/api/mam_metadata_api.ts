import api from '@/utils/api'
import { MATERIAL_SERVICE_PATH } from './constants'

const PREFIX = `${MATERIAL_SERVICE_PATH}/api/material/metadata`

export interface MaterialMetadataTemplateVO {
  id: string
  name: string
  catalogId: string
  resourceType?: number | null
  enabled: number
}

export interface MaterialMetadataTemplateUpsertDTO {
  id?: string
  name: string
  catalogId: string
  resourceType?: number | null
  enabled?: number
}

export interface MaterialMetadataTemplateQueryDTO {
  catalogId?: string
  resourceType?: number
  enabledOnly?: boolean
}

export interface MaterialMetadataFieldVO {
  id: string
  fieldCode: string
  fieldName: string
  inputType: string
  required?: number
  optionsJson?: string
}

export interface MaterialMetadataFieldUpsertDTO {
  id?: string
  fieldCode: string
  fieldName: string
  inputType: string
  required?: number
  optionsJson?: string
}

export interface MaterialMetadataFieldBindItemDTO {
  fieldId: string
  sortNum?: number
}

export interface MaterialMetadataTemplateBindFieldsDTO {
  templateId: string
  bindings: MaterialMetadataFieldBindItemDTO[]
}

export interface MaterialMetadataFormFieldVO {
  fieldId: string
  fieldCode: string
  fieldName: string
  inputType: string
  required?: number
  optionsJson?: string
  sortNum?: number
}

export interface MaterialMetadataSaveDTO {
  resourceId: string
  templateId: string
  fieldValues: Record<string, string>
}

export interface MaterialMetadataSnapshotVO {
  templateId: string
  templateName: string
  version: number | null
  entries: MaterialMetadataInstanceEntryVO[]
}

export interface MaterialMetadataInstanceEntryVO {
  fieldId: string
  fieldCode: string
  fieldName: string
  fieldValue: string
  version?: number
  lastVersion?: number
  templateId?: string
}

export function createTemplate(data: MaterialMetadataTemplateUpsertDTO): Promise<MaterialMetadataTemplateVO> {
  return api.post(`${PREFIX}/template`, data)
}

export function updateTemplate(data: MaterialMetadataTemplateUpsertDTO): Promise<MaterialMetadataTemplateVO> {
  return api.put(`${PREFIX}/template`, data)
}

export function deleteTemplate(id: string): Promise<void> {
  return api.delete(`${PREFIX}/template`, { params: { id } })
}

export function listTemplates(query?: MaterialMetadataTemplateQueryDTO): Promise<MaterialMetadataTemplateVO[]> {
  return api.get(`${PREFIX}/template/list`, { params: query || {} })
}

export function bindTemplateFields(data: MaterialMetadataTemplateBindFieldsDTO): Promise<void> {
  return api.put(`${PREFIX}/template/fields`, data)
}

export function listTemplateBindings(templateId: string): Promise<MaterialMetadataFormFieldVO[]> {
  return api.get(`${PREFIX}/template/bindings`, { params: { templateId } })
}

export function createField(data: MaterialMetadataFieldUpsertDTO): Promise<MaterialMetadataFieldVO> {
  return api.post(`${PREFIX}/field`, data)
}

export function updateField(data: MaterialMetadataFieldUpsertDTO): Promise<MaterialMetadataFieldVO> {
  return api.put(`${PREFIX}/field`, data)
}

export function deleteField(id: string): Promise<void> {
  return api.delete(`${PREFIX}/field`, { params: { id } })
}

export function listFields(): Promise<MaterialMetadataFieldVO[]> {
  return api.get(`${PREFIX}/field/list`)
}

export function formFieldsForResource(resourceId: string, templateId: string): Promise<MaterialMetadataFormFieldVO[]> {
  return api.get(`${PREFIX}/instance/form-fields`, { params: { resourceId, templateId } })
}

export function saveInstance(data: MaterialMetadataSaveDTO): Promise<MaterialMetadataSnapshotVO> {
  return api.post(`${PREFIX}/instance/save`, data)
}

export function getLastMetadata(resourceId: string, templateId: string): Promise<MaterialMetadataSnapshotVO> {
  return api.get(`${PREFIX}/instance/last`, { params: { resourceId, templateId } })
}

export function metadataHistory(
  resourceId: string,
  templateId: string,
  maxVersion: number
): Promise<MaterialMetadataInstanceEntryVO[]> {
  return api.get(`${PREFIX}/instance/history`, { params: { resourceId, templateId, maxVersion } })
}
