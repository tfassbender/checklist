import { useState, useEffect, useCallback } from 'react'
import { CheckList, CheckListSummary, CheckListItem } from '../types'
import * as api from '../services/api'

export function useLists() {
  const [lists, setLists] = useState<CheckListSummary[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetchLists = useCallback(async () => {
    try {
      setIsLoading(true)
      setError(null)
      const data = await api.getLists()
      setLists(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch lists')
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    fetchLists()
  }, [fetchLists])

  const createList = useCallback(async (name: string) => {
    const newList = await api.createList({ name })
    setLists(prev => [
      ...prev,
      {
        id: newList.id,
        name: newList.name,
        itemCount: 0,
        checkedCount: 0,
        active: newList.active,
        updatedAt: newList.updatedAt,
      },
    ])
    return newList
  }, [])

  const deleteList = useCallback(async (id: string) => {
    await api.deleteList(id)
    setLists(prev => prev.filter(list => list.id !== id))
  }, [])

  return {
    lists,
    isLoading,
    error,
    fetchLists,
    createList,
    deleteList,
  }
}

export function useList(id: string | undefined) {
  const [list, setList] = useState<CheckList | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const fetchList = useCallback(async () => {
    if (!id) return
    try {
      setIsLoading(true)
      setError(null)
      const data = await api.getList(id)
      setList(data)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch list')
    } finally {
      setIsLoading(false)
    }
  }, [id])

  useEffect(() => {
    fetchList()
  }, [fetchList])

  const updateList = useCallback(
    async (updates: { name?: string; notes?: string }) => {
      if (!id || !list) return
      const updated = await api.updateList(id, updates)
      setList(updated)
      return updated
    },
    [id, list]
  )

  const resetList = useCallback(async () => {
    if (!id) return
    const updated = await api.resetList(id)
    setList(updated)
    return updated
  }, [id])

  const addItem = useCallback(
    async (description: string) => {
      if (!id || !list) return
      const newItem = await api.addItem(id, { description })
      setList(prev =>
        prev
          ? {
              ...prev,
              items: [...prev.items, newItem],
              updatedAt: new Date().toISOString(),
            }
          : null
      )
      return newItem
    },
    [id, list]
  )

  const updateItem = useCallback(
    async (itemId: string, updates: { description?: string; checked?: boolean }) => {
      if (!id || !list) return
      const updatedItem = await api.updateItem(id, itemId, updates)
      setList(prev =>
        prev
          ? {
              ...prev,
              items: prev.items.map(item =>
                item.id === itemId ? updatedItem : item
              ),
              updatedAt: new Date().toISOString(),
            }
          : null
      )
      return updatedItem
    },
    [id, list]
  )

  const deleteItem = useCallback(
    async (itemId: string) => {
      if (!id || !list) return
      await api.deleteItem(id, itemId)
      setList(prev =>
        prev
          ? {
              ...prev,
              items: prev.items.filter(item => item.id !== itemId),
              updatedAt: new Date().toISOString(),
            }
          : null
      )
    },
    [id, list]
  )

  const reorderItems = useCallback(
    async (itemIds: string[]) => {
      if (!id || !list) return
      const updated = await api.reorderItems(id, { itemIds })
      setList(updated)
      return updated
    },
    [id, list]
  )

  const optimisticReorder = useCallback(
    (reorderedItems: CheckListItem[]) => {
      setList(prev =>
        prev
          ? {
              ...prev,
              items: reorderedItems,
            }
          : null
      )
    },
    []
  )

  return {
    list,
    isLoading,
    error,
    fetchList,
    updateList,
    resetList,
    addItem,
    updateItem,
    deleteItem,
    reorderItems,
    optimisticReorder,
  }
}
