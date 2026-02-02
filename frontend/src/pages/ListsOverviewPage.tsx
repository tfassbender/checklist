import { useState } from 'react'
import { useLists } from '../hooks/useLists'
import ListCard from '../components/ListCard'
import ConfirmDialog from '../components/ConfirmDialog'

export default function ListsOverviewPage() {
  const { lists, isLoading, error, createList, deleteList, fetchLists } = useLists()
  const [isCreating, setIsCreating] = useState(false)
  const [newListName, setNewListName] = useState('')
  const [createError, setCreateError] = useState<string | null>(null)
  const [deleteTarget, setDeleteTarget] = useState<{ id: string; name: string } | null>(null)

  const handleCreateList = async (e: React.FormEvent) => {
    e.preventDefault()
    const name = newListName.trim()
    if (!name) return

    setCreateError(null)
    try {
      await createList(name)
      setNewListName('')
      setIsCreating(false)
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Failed to create list'
      setCreateError(message)
    }
  }

  const handleDeleteList = async () => {
    if (!deleteTarget) return
    try {
      await deleteList(deleteTarget.id)
      setDeleteTarget(null)
    } catch (err) {
      console.error('Failed to delete list:', err)
    }
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[50vh]">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary-600"></div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="max-w-4xl mx-auto p-4">
        <div className="card p-6 text-center">
          <p className="text-red-600 dark:text-red-400 mb-4">{error}</p>
          <button onClick={fetchLists} className="btn btn-primary">
            Try Again
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-4xl mx-auto p-4 pb-24">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">
          My Lists
        </h1>
        <p className="text-gray-600 dark:text-gray-400 mt-1">
          {lists.length === 0
            ? 'Create your first checklist'
            : `${lists.length} list${lists.length === 1 ? '' : 's'}`}
        </p>
      </div>

      {isCreating && (
        <div className="card p-4 mb-4">
          <form onSubmit={handleCreateList}>
            {createError && (
              <div className="mb-3 p-3 rounded-lg bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-400 text-sm">
                {createError}
              </div>
            )}
            <input
              type="text"
              value={newListName}
              onChange={e => setNewListName(e.target.value)}
              placeholder="List name"
              className="input mb-3"
              autoFocus
            />
            <div className="flex justify-end gap-2">
              <button
                type="button"
                onClick={() => {
                  setIsCreating(false)
                  setNewListName('')
                  setCreateError(null)
                }}
                className="btn btn-secondary"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={!newListName.trim()}
                className="btn btn-primary"
              >
                Create
              </button>
            </div>
          </form>
        </div>
      )}

      {lists.length === 0 && !isCreating ? (
        <div className="card p-8 text-center">
          <svg
            className="w-16 h-16 mx-auto mb-4 text-gray-400 dark:text-gray-500"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="1.5"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <path d="M9 11l3 3L22 4" />
            <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
          </svg>
          <h3 className="text-lg font-medium text-gray-900 dark:text-gray-100 mb-2">
            No lists yet
          </h3>
          <p className="text-gray-600 dark:text-gray-400 mb-4">
            Create your first checklist to get started
          </p>
          <button onClick={() => setIsCreating(true)} className="btn btn-primary">
            Create List
          </button>
        </div>
      ) : (
        <div className="grid gap-4 sm:grid-cols-2">
          {lists.map(list => (
            <ListCard
              key={list.id}
              list={list}
              onDelete={id => {
                const target = lists.find(l => l.id === id)
                if (target) {
                  setDeleteTarget({ id, name: target.name })
                }
              }}
            />
          ))}
        </div>
      )}

      {!isCreating && lists.length > 0 && (
        <button
          onClick={() => setIsCreating(true)}
          className="fixed bottom-6 right-6 w-14 h-14 rounded-full bg-primary-600 text-white
                     shadow-lg hover:bg-primary-700 focus:outline-none focus:ring-2
                     focus:ring-primary-500 focus:ring-offset-2 flex items-center justify-center"
          title="Create new list"
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
            <line x1="12" y1="5" x2="12" y2="19" />
            <line x1="5" y1="12" x2="19" y2="12" />
          </svg>
        </button>
      )}

      <ConfirmDialog
        isOpen={!!deleteTarget}
        title="Delete List"
        message={`Are you sure you want to delete "${deleteTarget?.name}"? This action cannot be undone.`}
        confirmText="Delete"
        confirmVariant="danger"
        onConfirm={handleDeleteList}
        onCancel={() => setDeleteTarget(null)}
      />
    </div>
  )
}
