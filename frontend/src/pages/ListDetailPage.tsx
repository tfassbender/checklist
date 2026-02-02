import { useState, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  DragEndEvent,
} from '@dnd-kit/core'
import {
  arrayMove,
  SortableContext,
  sortableKeyboardCoordinates,
  verticalListSortingStrategy,
} from '@dnd-kit/sortable'
import { useList } from '../hooks/useLists'
import CheckListItemComponent from '../components/CheckListItem'
import EditableItem from '../components/EditableItem'
import NotesSection from '../components/NotesSection'
import ConfirmDialog from '../components/ConfirmDialog'

export default function ListDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const {
    list,
    isLoading,
    error,
    updateList,
    resetList,
    addItem,
    updateItem,
    deleteItem,
    reorderItems,
    optimisticReorder,
  } = useList(id)

  const [editMode, setEditMode] = useState(false)
  const [editingItemId, setEditingItemId] = useState<string | null>(null)
  const [newItemText, setNewItemText] = useState('')
  const [isEditingName, setIsEditingName] = useState(false)
  const [showResetConfirm, setShowResetConfirm] = useState(false)

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  )

  const handleDragEnd = useCallback(
    async (event: DragEndEvent) => {
      const { active, over } = event
      if (!over || !list || active.id === over.id) return

      const oldIndex = list.items.findIndex(item => item.id === active.id)
      const newIndex = list.items.findIndex(item => item.id === over.id)

      if (oldIndex !== -1 && newIndex !== -1) {
        const reorderedItems = arrayMove(list.items, oldIndex, newIndex)
        optimisticReorder(reorderedItems)
        const itemIds = reorderedItems.map(item => item.id)
        await reorderItems(itemIds)
      }
    },
    [list, optimisticReorder, reorderItems]
  )

  const handleAddItem = async (e: React.FormEvent) => {
    e.preventDefault()
    const description = newItemText.trim()
    if (!description) return

    await addItem(description)
    setNewItemText('')
  }

  const handleToggleItem = async (itemId: string, checked: boolean) => {
    await updateItem(itemId, { checked })
  }

  const handleEditItem = async (itemId: string, description: string) => {
    await updateItem(itemId, { description })
    setEditingItemId(null)
  }

  const handleDeleteItem = async (itemId: string) => {
    await deleteItem(itemId)
  }

  const handleReset = async () => {
    await resetList()
    setShowResetConfirm(false)
  }

  const handleUpdateName = async (name: string) => {
    await updateList({ name })
    setIsEditingName(false)
  }

  const handleUpdateNotes = async (notes: string) => {
    await updateList({ notes })
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[50vh]">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    )
  }

  if (error || !list) {
    return (
      <div className="max-w-2xl mx-auto p-4">
        <div className="card p-6 text-center">
          <p className="text-red-600 dark:text-red-400 mb-4">
            {error || 'List not found'}
          </p>
          <button onClick={() => navigate('/')} className="btn btn-primary">
            Back to Lists
          </button>
        </div>
      </div>
    )
  }

  const checkedCount = list.items.filter(item => item.checked).length
  const totalCount = list.items.length
  const allChecked = totalCount > 0 && checkedCount === totalCount

  return (
    <div className="max-w-2xl mx-auto p-4 pb-24">
      {/* Header */}
      <div className="flex items-center gap-3 mb-6">
        <button
          onClick={() => navigate('/')}
          className="btn-icon"
          title="Back to lists"
        >
          <svg
            className="w-6 h-6"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <line x1="19" y1="12" x2="5" y2="12" />
            <polyline points="12 19 5 12 12 5" />
          </svg>
        </button>

        <div className="flex-1">
          {isEditingName ? (
            <EditableItem
              value={list.name}
              onSave={handleUpdateName}
              onCancel={() => setIsEditingName(false)}
              placeholder="List name"
            />
          ) : (
            <button
              onClick={() => editMode && setIsEditingName(true)}
              className={`text-xl font-bold text-gray-900 dark:text-gray-100 text-left
                         ${editMode ? 'hover:text-primary-600 cursor-pointer' : 'cursor-default'}`}
            >
              {list.name}
            </button>
          )}
          <div className="text-sm text-gray-600 dark:text-gray-400">
            {totalCount === 0
              ? 'No items'
              : `${checkedCount}/${totalCount} completed`}
          </div>
        </div>

        <button
          onClick={() => setEditMode(!editMode)}
          className={`btn-icon ${editMode ? 'bg-primary-100 dark:bg-primary-900/30 text-primary-600' : ''}`}
          title={editMode ? 'Exit edit mode' : 'Enter edit mode'}
        >
          <svg
            className="w-5 h-5"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
          </svg>
        </button>
      </div>

      {/* Notes Section */}
      <div className="mb-6">
        <NotesSection notes={list.notes} onSave={handleUpdateNotes} />
      </div>

      {/* Items List */}
      <div className="space-y-2 mb-6">
        <DndContext
          sensors={sensors}
          collisionDetection={closestCenter}
          onDragEnd={handleDragEnd}
        >
          <SortableContext
            items={list.items.map(item => item.id)}
            strategy={verticalListSortingStrategy}
          >
            {list.items.map(item =>
              editingItemId === item.id ? (
                <div key={item.id} className="card p-3">
                  <EditableItem
                    value={item.description}
                    onSave={description => handleEditItem(item.id, description)}
                    onCancel={() => setEditingItemId(null)}
                    placeholder="Item description"
                  />
                </div>
              ) : (
                <CheckListItemComponent
                  key={item.id}
                  item={item}
                  editMode={editMode}
                  onToggle={handleToggleItem}
                  onEdit={setEditingItemId}
                  onDelete={handleDeleteItem}
                />
              )
            )}
          </SortableContext>
        </DndContext>

        {list.items.length === 0 && (
          <div className="card p-8 text-center">
            <p className="text-gray-500 dark:text-gray-400">
              No items yet. Add your first item below.
            </p>
          </div>
        )}
      </div>

      {/* Add Item */}
      {editMode && (
        <form onSubmit={handleAddItem} className="flex gap-2 mb-6">
          <input
            type="text"
            value={newItemText}
            onChange={e => setNewItemText(e.target.value)}
            placeholder="Add new item..."
            className="input flex-1"
            autoFocus
          />
          <button
            type="submit"
            disabled={!newItemText.trim()}
            className="btn btn-primary"
          >
            Add
          </button>
        </form>
      )}

      {/* Bottom Action Bar */}
      {totalCount > 0 && (
        <div className="fixed bottom-0 left-0 right-0 bg-white dark:bg-gray-800
                        border-t border-gray-200 dark:border-gray-700 p-4">
          <div className="max-w-2xl mx-auto flex items-center justify-between">
            <div className="flex items-center gap-2">
              {allChecked && (
                <span className="text-green-600 dark:text-green-400 flex items-center gap-1">
                  <svg
                    className="w-5 h-5"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
                    <polyline points="22 4 12 14.01 9 11.01" />
                  </svg>
                  All done!
                </span>
              )}
            </div>
            <button
              onClick={() => setShowResetConfirm(true)}
              className="btn btn-secondary"
              disabled={checkedCount === 0}
            >
              Reset
            </button>
          </div>
        </div>
      )}

      <ConfirmDialog
        isOpen={showResetConfirm}
        title="Reset List"
        message="This will uncheck all items in the list. Are you sure?"
        confirmText="Reset"
        onConfirm={handleReset}
        onCancel={() => setShowResetConfirm(false)}
      />
    </div>
  )
}
